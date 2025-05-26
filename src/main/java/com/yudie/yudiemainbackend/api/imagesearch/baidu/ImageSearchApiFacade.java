package com.yudie.yudiemainbackend.api.imagesearch.baidu;


import com.yudie.yudiemainbackend.api.imagesearch.baidu.model.ImageSearchResult;
import com.yudie.yudiemainbackend.api.imagesearch.baidu.sub.GetImageFirstUrlApi;
import com.yudie.yudiemainbackend.api.imagesearch.baidu.sub.GetImageListApi;
import com.yudie.yudiemainbackend.api.imagesearch.baidu.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://i1.hdslb.com/bfs/face/21d51a3bb52d87d938c2fe8e56bf17de2a8ef3c6.jpg";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
