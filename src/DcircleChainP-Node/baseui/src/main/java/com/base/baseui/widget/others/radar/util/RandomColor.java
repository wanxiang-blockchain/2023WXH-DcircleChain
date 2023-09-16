package com.base.baseui.widget.others.radar.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * @author changhai qiu
 *         Email:qstumn@163.com
 */
public class RandomColor {
    private final HashMap<Integer, Integer> BrightColor;
    private final HashMap<Integer, Integer> LightColor;

    {
        BrightColor = new HashMap<>();
        LightColor = new HashMap<>();
        LightColor.put(0xFF89FAB5, 0);
//        LightColor.put(0xffec407a, 0);

    }

    public int randomColor() {
        ArrayList<Integer> color = new ArrayList<>(LightColor.keySet());
        int randomColor;
        int i = 0;
        int count = 0;
        do {
            randomColor = color.get(new Random().nextInt(color.size()));
            count++;
            if (count > color.size()) {
                i++;
            }
        } while (LightColor.get(randomColor) != i);
        LightColor.put(randomColor, LightColor.get(randomColor) + 1);
        return randomColor;
    }
}
