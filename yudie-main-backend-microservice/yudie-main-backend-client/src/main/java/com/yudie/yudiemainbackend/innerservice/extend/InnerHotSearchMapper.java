package com.yudie.yudiemainbackend.innerservice.extend;


import com.yudie.yudiemainbackend.model.entity.HotSearch;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface InnerHotSearchMapper {

    List<HotSearch> getHotSearchAfter(@Param("type") String type,
                                      @Param("startTime") Date startTime,
                                      @Param("limit") Integer limit);
}
