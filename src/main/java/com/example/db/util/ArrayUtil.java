package com.example.db.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ArrayUtil {
    /**
     * 从count个连续数字(0开始)中，选出不重复的n项
     */
    public static List<Integer> selectN1(int count, int n) {
        LinkedList<Integer> selectList = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            selectList.add(i);
        }
        ArrayList<Integer> resultList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            int index = random.nextInt(selectList.size());
            resultList.add(selectList.remove(index));
        }
        return resultList;
    }

    /**
     * 从count个连续数字(0开始)中，选出不重复的n项
     */
    public static int[] selectN2(int count, int n) {
        LinkedList<Integer> selectList = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            selectList.add(i);
        }
        Collections.shuffle(selectList);
        int[] dest = new int[n];
        for (int i = 0; i < dest.length; i++) {
            dest[i] = selectList.get(i);
        }
        return dest;
    }

    /**
     * 从指定范围中选取N个数
     * 
     * @param min 最小范围
     * @param max 最大范围
     * @param n   选取的个数
     */
    public static Integer[] selectN3(int min, int max, int n) {
        if (n > (max - min + 1) || max < min)
            return null;
        Random rand = new Random();
        Integer[] result = new Integer[n];
        int count = 0;
        while (count < n) {
            // MIN 和 MAX 范围内的随机数
            int num = rand.nextInt(max - min + 1) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if (null != result[j] && num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    /**
     * 从指定范围中选取N个数
     * 
     * @param min 最小范围
     * @param max 最大范围
     * @param n   选取的个数
     */
    public static void selectN4(int min, int max, int n, HashSet<Integer> set) {
        if (n > (max - min + 1) || max < min)
            return;
        for (int i = 0; i < n; i++) {
            // 调用Math.random()方法
            int num = (int) (Math.random() * (max - min)) + min;
            // 将不同的数存入HashSet中
            set.add(num);
        }
        int setSize = set.size();
        // 如果存入的数小于指定生成的个数，则调用递归再生成剩余个数的随机数，如此循环，直到达到指定大小
        if (setSize < n) {
            // 递归
            selectN4(min, max, n - setSize, set);
        }
    }
}
