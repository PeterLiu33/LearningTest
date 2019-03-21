package com.koflance.lt.common;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;

/**
 * Created by liujun on 2019/3/21.
 */
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    private static ThreadLocal<DecimalFormat> decimalFormat=new ThreadLocal<DecimalFormat>(){
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("#,###.#");
        }
    };

    /**
     * 区间值 [0.5, 1.5)
     * @param valueMap 例如 0.5~1.5
     * @param value
     * @return
     */
    public static String matchInRange(Map<String, String> valueMap, float value){
        try{
            Optional<Map.Entry<String, String>> first = valueMap.entrySet().stream().filter((stringStringEntry -> {
                String key = stringStringEntry.getKey();
                String[] split = StringUtils.split(key, "~");
                if (split == null || split.length != 2) {
                    return false;
                }
                Float aFloat = Float.valueOf(split[0]);
                if (value < aFloat) {
                    return false;
                }
                aFloat = Float.valueOf(split[1]);
                if (value >= aFloat) {
                    return false;
                }
                return true;
            })).findFirst();
            if(first.isPresent()){
                return first.get().getValue();
            }
        }catch (Exception e){
            logger.error("mock data for trade guess,exception:{}",e);
        }
        return StringUtils.EMPTY;
    }

    public static String getCount(String value){
        if (NumberUtils.isNumber(value)) {
            try {
                double count = Double.parseDouble(value);
                String unit = "";
                if (count >= 1_0000_0000){
                    unit="亿";
                    count /= 1_0000_0000;
                }else if (count >= 1_0000) {
                    unit = "万";
                    count /= 1_0000;
                }
                String priceStr = decimalFormat.get().format(count);
                return priceStr + unit;
            } catch (Exception e){
                logger.error("CommonUtil getCount, exception:{}",e);
            }
        }
        return StringUtils.EMPTY;
    }
}
