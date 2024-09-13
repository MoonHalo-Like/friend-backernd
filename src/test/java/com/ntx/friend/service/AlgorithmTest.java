package com.ntx.friend.service;

import com.ntx.friend.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName AlgorithmUtils
 * @Author ntx
 * @Description 算法工具类测试
 * @Date 2024/7/29 11:03
 */
@SpringBootTest
public class AlgorithmTest {
    @Test
    void test(){
        List<String> tagList1 = Arrays.asList("java","大一","男","乒乓球");
        List<String> tagList2 = Arrays.asList("java","大三","女","乒乓球");
        List<String> tagList3 = Arrays.asList("java","大四","女","编程");

        System.out.println(AlgorithmUtils.minDistance(tagList1, tagList2));
        System.out.println(AlgorithmUtils.minDistance(tagList1, tagList3));


    }
}
