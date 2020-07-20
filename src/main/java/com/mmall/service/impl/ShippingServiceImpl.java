package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("shippingService")
public class ShippingServiceImpl implements ShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int count = shippingMapper.insert(shipping);
        if(count > 0) {
            Map res= Maps.newHashMap();
            res.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功", res);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse<String> delete(Integer userId, Integer shippingId) {
        int count = shippingMapper.deleteByShippingIdAndUserId(userId, shippingId);
        if(count > 0) {
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int count = shippingMapper.updateByShipping(shipping);
        if(count > 0) {
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByShippingIdAndUserId(userId, shippingId);
        if(shipping == null) {
            return ServerResponse.createByErrorMessage("无法查询该地址");
        }
        return ServerResponse.createBySuccess("查询地址成功", shipping);
    }

    public ServerResponse<PageInfo> selectAll(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippings = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippings);
        return ServerResponse.createBySuccessData(pageInfo);
    }




}
