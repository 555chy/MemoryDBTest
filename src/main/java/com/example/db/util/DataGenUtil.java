package com.example.db.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class DataGenUtil {
    /** 姓 */
    private static final String[] SURNAME = { "赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫", "蒋", "沈", "韩",
            "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶", "姜", "戚", "谢", "邹", "喻",
            "柏", "水", "窦", "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "郎", "鲁", "韦", "昌", "马", "苗", "凤", "花", "方", "俞",
            "任", "袁", "柳", "酆", "鲍", "史", "唐", "费", "廉", "岑", "薛", "雷", "贺", "倪", "汤", "滕", "殷", "罗", "毕", "郝", "邬",
            "安", "常", "乐", "于", "时", "傅", "皮", "卞", "齐", "康", "伍", "余", "元", "卜", "顾", "孟", "平", "黄", "和", "穆", "萧",
            "尹", "姚", "邵", "湛", "汪", "祁", "毛", "禹", "狄", "米", "贝", "明", "臧", "计", "伏", "成", "戴", "谈", "宋", "茅", "庞",
            "熊", "纪", "舒", "屈", "项", "祝", "董", "梁", "杜", "阮", "蓝", "闵", "席", "季" };
    /** 男孩名 */
    private static final String NAME_BOY = "伟刚勇毅俊峰强军平保东文辉力明永健世广志义兴良海山仁波宁贵福生龙元全国胜学祥才发武新利清飞彬富顺信子杰涛昌成康星光天达安岩中茂进林有坚和彪博诚先敬震振壮会思群豪心邦承乐绍功松善厚庆磊民友裕河哲江超浩亮政谦亨奇固之轮翰朗伯宏言若鸣朋斌梁栋维启克伦翔旭鹏泽晨辰士以建家致树炎德行时泰盛雄琛钧冠策腾楠榕风航弘";
    /** 女孩名 */
    private static final String NAME_GIRL = "秀娟英华慧巧美娜静淑惠珠翠雅芝玉萍红娥玲芬芳燕彩春菊兰凤洁梅琳素云莲真环雪荣爱妹霞香月莺媛艳瑞凡佳嘉琼勤珍贞莉桂娣叶璧璐娅琦晶妍茜秋珊莎锦黛青倩婷姣婉娴瑾颖露瑶怡婵雁蓓纨仪荷丹蓉眉君琴蕊薇菁梦岚苑婕馨瑗琰韵融园艺咏卿聪澜纯毓悦昭冰爽琬茗羽希宁欣飘育滢馥筠柔竹霭凝晓欢霄枫芸菲寒伊亚宜可姬舒影荔枝思丽";
    /** 最大名字长度 */
    private static final int NAME_LEN = 2;

    /** 给予真实的初始号段，号段是在百度上面查找的真实号段 */
    private static final String[] PHONE_START = { "133", "149", "153", "173", "177", "180", "181", "189", "199", "130",
            "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166", "134", "135", "136",
            "137", "138", "139", "147", "150", "151", "152", "157", "158", "159", "172", "178", "182", "183", "184",
            "187", "188", "198", "170", "171" };

    /** 34个省份包含23个省", "5个自治区", "4个直辖市", "2个特别行政区 */
    private static final String[] PROVINCES = { "河北省", "山西省", "辽宁省", "吉林省", "黑龙江省", "江苏省", "浙江省", "安徽省", "福建省", "江西省",
            "山东省", "河南省", "湖北省", "湖南省", "广东省", "海南省", "四川省", "贵州省", "云南省", "陕西省", "甘肃省", "青海省", "台湾省", "内蒙古自治区",
            "广西壮族自治区", "西藏自治区", "宁夏回族自治区", "新疆维吾尔自治区", "北京市", "天津市", "上海市", "重庆市", "香港特别行政区", "澳门特别行政区" };

    /** 年级 */
    private static final String[] GRADES = { "一年级", "二年级", "三年级", "四年级", "五年级", "六年级", "初一", "初二", "初三", "高一", "高二",
            "高三", "大一", "大二", "大三", "大四" };

    /** 中文版数字 */
    private static final String[] CHINESE_NUM = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十" };

    /** 爱好 */
    private static final String[] HOBBY = { "篮球", "乒乓球", "足球", "羽毛球", "游泳", "健身", "K歌", "跳舞", "弹钢琴", "打游戏", "看片" };
    private static final String HOBBY_SEPARATOR = "、";

    /** 地区 */
    private static final String[] DISTRICT = { "鼓楼区", "台江区", "晋安区", "仓山区", "马尾区" };

    /** 随机数 */
    private static Random random = new Random();

    /**
     * 随机性别
     */
    public static byte sex() {
        return sex(isMale());
    }

    /**
     * 随机性别
     */
    public static byte sex(boolean male) {
        return male ? (byte) 1 : 0;
    }

    /**
     * 随机性别
     */
    public static boolean isMale() {
        return random.nextBoolean();
    }

    /**
     * 使用当前性别随机名字
     */
    public static String name(boolean male) {
        int index = random.nextInt(SURNAME.length - 1);
        String name = SURNAME[index];
        // 根据之前计算好的性别，随机名字
        String arr = male ? NAME_BOY : NAME_GIRL;
        int len = random.nextInt(NAME_LEN);
        for (int i = 0; i <= len; i++) {
            name += arr.charAt(random.nextInt(arr.length()));
        }
        return name;
    }

    /**
     * 随机16字节的UUID
     */
    public static UUID uuid() {
        return UUID.randomUUID();
    }

    /**
     * 随机生成年龄
     */
    public static int age() {
        Random random = new Random();
        return random.nextInt(100);
    }

    /**
     * 先前某年的时间戳（月份随机）
     */
    public static Timestamp birthday(int age) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.add(Calendar.YEAR, -1 * age);
        calendar.set(Calendar.MONTH, random.nextInt(Calendar.UNDECIMBER));
        calendar.set(Calendar.DATE, random.nextInt(28) + 1);
        long timestamp = calendar.getTimeInMillis();
        return new Timestamp(timestamp);
    }

    /**
     * 随机身高(100-190)，并用身高换算标准体重
     */
    public static float height() {
        return (random.nextInt(100) + 100) / 100f;
    }

    /**
     * 并用身高换算标准体重
     */
    public static int weight(float height) {
        return (int) ((height -1 ) * 100 * 0.9f);
    }

    /**
     * 随机生成手机号码
     */
    public static String phone() {
        // 随机出真实号段 使用数组的length属性，获得数组长度，
        // 通过Math.random（）*数组长度获得数组下标，从而随机出前三位的号段
        String phoneFirstNum = PHONE_START[(int) (Math.random() * PHONE_START.length)];
        // 随机出剩下的8位数
        String phoneLastNum = "";
        // 定义尾号，尾号是8位
        final int LENPHONE = 8;
        // 循环剩下的位数
        for (int i = 0; i < LENPHONE; i++) {
            // 每次循环都从0~9挑选一个随机数
            phoneLastNum += (int) (Math.random() * 10);
        }
        // 最终将号段和尾数连接起来
        return phoneFirstNum + phoneLastNum;
    }

    /**
     * 随机一个省份
     */
    public static String province() {
        Random random = new Random();
        return PROVINCES[random.nextInt(PROVINCES.length)];
    }

    /**
     * 随机区域
     */
    public static String distinct() {
        return DISTRICT[random.nextInt(DISTRICT.length)];
    }

    /**
     * 随机爱好个数
     */
    public static int hobbyCount() {
        return random.nextInt(HOBBY.length) + 1;
    }

    /**
     * 随机一组多个兴趣爱好
     */
    public static String hobbies(int count) {
        List<Integer> selectList = ArrayUtil.selectN1(HOBBY.length, count);
        // 有几个爱好
        String hobby = "";
        for (int i = 0; i < count; i++) {
            if (i != 0)
                hobby += HOBBY_SEPARATOR;
            hobby += HOBBY[selectList.get(i)];
        }
        return hobby;
    }

    /**
     * 随机工资（单位：元）
     */
    public static float wages() {
        Random random = new Random();
        return random.nextInt(20000 * 100) / 100f;
    }

    /**
     * 月销量 ##.##
     */
    public static float monthSales() {
        int value = random.nextInt(100);
        return value + value / 100.0f;
    }

    /**
     * 总销售额（##.## * 12）
     */
    public static BigDecimal turnover() {
        int value = random.nextInt(100);
        return new BigDecimal(value + value / 100.0 * 12).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 随机学校
     */
    public static String school(String province) {
        Random random = new Random();
        int value = random.nextInt(40) + 1;
        int high = value / 10;
        int low = value % 10;
        StringBuilder sb = new StringBuilder();
        sb.append(province);
        if (high != 0) {
            sb.append(CHINESE_NUM[high]);
            sb.append(CHINESE_NUM[10]);
        }
        if (low != 0)
            sb.append(CHINESE_NUM[low]);
        sb.append("中");
        return sb.toString();
    }

    /**
     * 随机年级
     */
    public static String grade() {
        return GRADES[random.nextInt(GRADES.length)];
    }

    /**
     * 随机网址
     */
    public static String website() {
        StringBuilder sb = new StringBuilder("https://www.");
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            char c = (char) ('a' + random.nextInt(26));
            sb.append(c);
        }
        sb.append(".com/");
        for (int i = 0; i < 5; i++) {
            char c = (char) ('a' + random.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 当前日期
     */
    public static Date currnetDate() {
        long current = System.currentTimeMillis();
        return new Date(current);
    }

    /**
     * 当前时间
     */
    public static Time currentTime() {
        long current = System.currentTimeMillis();
        return new Time(current);
    }

    /**
     * 当前时间戳
     */
    public static Timestamp current() {
        long current = System.currentTimeMillis();
        return new Timestamp(current);
    }

}
