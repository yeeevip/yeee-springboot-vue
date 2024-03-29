package vip.yeee.app.sys.manage.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import vip.yeee.app.sys.manage.convert.SysRoleConvert;
import vip.yeee.app.sys.manage.model.dto.SysUserRoleDto;
import vip.yeee.app.sys.manage.model.vo.SysRoleVO;
import vip.yeee.app.sys.manage.domain.mysql.mapper.SysRoleMapper;
import vip.yeee.app.sys.manage.domain.mysql.mapper.SysRoleMenuMapper;
import vip.yeee.app.sys.manage.domain.mysql.mapper.SysUserRoleMapper;
import vip.yeee.app.common.domain.mysql.entity.SysRole;
import vip.yeee.app.common.domain.mysql.entity.SysRoleMenu;
import vip.yeee.app.common.domain.mysql.entity.SysUserRole;
import vip.yeee.app.sys.manage.model.vo.SysRoleHasSetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.yeee.memo.base.model.exception.BizException;
import vip.yeee.memo.base.model.vo.PageVO;
import vip.yeee.memo.base.mybatisplus.warpper.MyPageWrapper;
import vip.yeee.memo.base.websecurityoauth2.context.SecurityContext;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description......
 *
 * @author yeeee
 * @since 2022/5/28 18:09
 */
@RequiredArgsConstructor
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRoleConvert sysRoleConvert;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMenuMapper sysRoleMenuMapper;

    public PageVO<SysRoleVO> sysRolePageList(String query) {
        MyPageWrapper<SysRole> pageWrapper = new MyPageWrapper<>(query);
        IPage<SysRole> page = this.page(pageWrapper.getPage(), pageWrapper.getQueryWrapper());
        List<SysRoleVO> roleVOList = page.getRecords()
                .stream()
                .map(sysRoleConvert::entity2VO)
                .collect(Collectors.toList());
        return new PageVO<>((int)page.getCurrent(), (int)page.getSize(), (int)page.getPages(), page.getTotal(), roleVOList);
    }

    public boolean sysRoleExist(String query) {
        MyPageWrapper<SysRole> pageWrapper = new MyPageWrapper<>(query);
        return this.count(pageWrapper.getQueryWrapper()) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public Void addSysRole(SysRoleVO editVO) {
        SysRole sysRole = sysRoleConvert.vo2Entity(editVO);
        this.save(sysRole);
        if (CollectionUtil.isNotEmpty(editVO.getMenuIdList())) {
            this.setSysRoleMenus(editVO.getMenuIdList(), sysRole.getId());
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public Void editSysRole(SysRoleVO editVO) {
        Integer roleId = editVO.getId();
        SysRole sysRole = this.getById(roleId);
        if (sysRole == null) {
            throw new BizException("角色不存在");
        }
        SysRole upd = sysRoleConvert.vo2Entity(editVO);
        this.updateById(upd);
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        if (CollectionUtil.isNotEmpty(editVO.getMenuIdList())) {
            this.setSysRoleMenus(editVO.getMenuIdList(), roleId);
        }
        return null;
    }

    private void setSysRoleMenus(List<Long> menuList, Integer roleId) {
        List<SysRoleMenu> roleMenuList = menuList
                .stream()
                .map(menu -> {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(roleId);
                    sysRoleMenu.setMenuId(menu);
                    String username = SecurityContext.getCurUser().getUsername();
                    Date date = new Date();
                    sysRoleMenu.setCreateTime(date);
                    sysRoleMenu.setCreateBy(username);
                    sysRoleMenu.setUpdateTime(date);
                    sysRoleMenu.setUpdateBy(username);
                    return sysRoleMenu;
                })
                .collect(Collectors.toList());
        sysRoleMenuMapper.batchInsert(roleMenuList);
    }

    public SysRoleVO sysRoleInfo(SysRoleVO editVO) {
        SysRole sysRole = this.getById(editVO.getId());
        if (sysRole == null) {
            throw new BizException("角色不存在");
        }
        return sysRoleConvert.entity2VO(sysRole);
    }

    public Void delSysRole(SysRoleVO editVO) {
        this.removeByIds(editVO.getIds());
        return null;
    }

    public SysRoleHasSetVO sysRoleListAndHasSet(Integer userId) {
        SysRoleHasSetVO roleHasSetVO = new SysRoleHasSetVO();
        List<SysUserRoleDto> userRoles = userId != null ? sysUserRoleMapper.getList(new SysUserRole().setUserId(userId)) : Collections.emptyList();
        roleHasSetVO.setCheckedKeys(Optional.ofNullable(userRoles).orElseGet(Lists::newArrayList)
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet()));
        List<SysRole> sysRoleList = this.list();
        List<SysRoleHasSetVO.RoleVO> roleVOList = sysRoleList
                .stream()
                .map(sysRoleConvert::entity2SetVO)
                .collect(Collectors.toList());
        roleHasSetVO.setList(roleVOList);
        return roleHasSetVO;
    }
}
