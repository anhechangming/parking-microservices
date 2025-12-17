package com.parking.user.service;

import com.parking.user.common.PageResult;
import com.parking.user.entity.Owner;
import com.parking.user.mapper.OwnerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 业主服务
 *
 * @author Parking System
 */
@Service
public class OwnerService {

    @Autowired
    private OwnerMapper ownerMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 分页查询业主列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词（姓名或手机号）
     * @return 业主分页数据
     */
    public PageResult<Owner> getOwnerPage(int pageNum, int pageSize, String keyword) {
        int offset = (pageNum - 1) * pageSize;
        List<Owner> records = ownerMapper.findByPage(offset, pageSize, keyword);
        int total = ownerMapper.countByKeyword(keyword);
        return new PageResult<>(pageNum, pageSize, total, records);
    }

    /**
     * 查询所有业主
     *
     * @return 业主列表
     */
    public List<Owner> getAllOwners() {
        return ownerMapper.findAll();
    }

    /**
     * 根据ID查询业主
     *
     * @param userId 业主ID
     * @return 业主信息
     */
    public Owner getOwnerById(Long userId) {
        return ownerMapper.findById(userId);
    }

    /**
     * 新增业主
     *
     * @param owner 业主信息
     * @return 是否成功
     */
    public boolean addOwner(Owner owner) {
        // 检查登录账号是否重复
        int count = ownerMapper.countByLoginName(owner.getLoginName());
        if (count > 0) {
            throw new RuntimeException("登录账号已存在");
        }

        // 加密密码
        if (StringUtils.hasText(owner.getPassword())) {
            owner.setPassword(passwordEncoder.encode(owner.getPassword()));
        } else {
            // 默认密码：admin123
            owner.setPassword(passwordEncoder.encode("admin123"));
        }

        return ownerMapper.insert(owner) > 0;
    }

    /**
     * 更新业主信息
     *
     * @param owner 业主信息
     * @return 是否成功
     */
    public boolean updateOwner(Owner owner) {
        return ownerMapper.update(owner) > 0;
    }

    /**
     * 删除业主
     *
     * @param userId 业主ID
     * @return 是否成功
     */
    public boolean deleteOwner(Long userId) {
        return ownerMapper.deleteById(userId) > 0;
    }

    /**
     * 修改业主密码
     *
     * @param userId 业主ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    public boolean changePassword(Long userId, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        return ownerMapper.updatePassword(userId, encodedPassword) > 0;
    }
}
