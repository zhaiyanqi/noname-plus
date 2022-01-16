package com.widget.noname.cola.function;

import android.view.ViewGroup;

import com.widget.noname.plus.common.function.BaseFunction;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FunctionManager {

    private final ConcurrentHashMap<String, BaseFunction> functionMap = new ConcurrentHashMap<>();

    private final ViewGroup container;

    public FunctionManager(ViewGroup container, List<FunctionBean> functions) {
        this.container = container;
        initFunctions(functions);
    }

    private void initFunctions(List<FunctionBean> functions) {
        functions.forEach(f -> {
            BaseFunction o = newInstance(f.getPath());

            if (o != null) {
                functionMap.put(f.getName(), o);
            }
        });
    }

    private BaseFunction newInstance(String classname) {
        try {
            Class<?> clazz = Class.forName(classname);
            Constructor<?> constructor = clazz.getConstructor(ViewGroup.class);
            return (BaseFunction) constructor.newInstance(container);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onResume() {
        functionMap.forEach((k, f) -> f.onResume());
    }

    public boolean checkToSwitch(String functionName) {
        BaseFunction baseFunction = functionMap.get(functionName);


        return baseFunction != null;
    }
}
