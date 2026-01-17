package io.github.timemachinelab.log.mdc;

import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MDCAdapter的初始化类，用于选择优先级最高的MDCAdapter并进行初始化
 *
 * @author glser
 * @since 2026/1/17
 */

public class TmlLogMdcInit implements InitializingBean {
    
    private final List<TmlLogMdc> mdcList;
    
    private volatile boolean initialized = false;
    
    public TmlLogMdcInit(List<TmlLogMdc> mdcList) {
        if (mdcList == null || mdcList.isEmpty()) {
            throw new IllegalArgumentException("Log mdcAdapter at least one");
        }
        this.mdcList = mdcList;
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (initialized) {
            return;
        }
        
        // 获取优先级最高的自定义 mdcAdapter 进行使用
        TmlLogMdc selectedMdc = selectMdc();

        MDCAdapter adapter = selectedMdc.adapter();
        if (adapter == null) {
            throw new IllegalArgumentException("TmlLogMdc adapter cannot be null");
        }
        
        // 通过反射获取当前的 TmlLogMdc
        MDCAdapter mdcAdapter = getMdcAdapter();

        // 获取当前 TmlLogMdc 的上下文 Map
        Map<String, String> copyOfContextMap = mdcAdapter.getCopyOfContextMap();
        
        // 反射替换mdcAdapter
        replaceMdcAdapter(adapter);
        
        if (copyOfContextMap != null && !copyOfContextMap.isEmpty()) {
            copyOfContextMap.forEach(MDC::put);
        }
        
        initialized = true;
    }

    private TmlLogMdc selectMdc() {
        List<TmlLogMdc> mdcAvailable = mdcList.stream()
                .filter(TmlLogMdc::isSupported)
                .collect(Collectors.toList());
        
        if (mdcAvailable.isEmpty()) {
            throw new IllegalStateException("No supported mdcAdapter found");
        }
        
        mdcAvailable.sort(AnnotationAwareOrderComparator.INSTANCE);

        return mdcAvailable.get(0);
    }
    
    private MDCAdapter getMdcAdapter() throws NoSuchFieldException, IllegalAccessException {
        Class<?> mdcClass = MDC.class;
        Field mdcAdapterField = mdcClass.getDeclaredField("mdcAdapter");
        mdcAdapterField.setAccessible(true);
        return (MDCAdapter) mdcAdapterField.get(null);
    }

    private void replaceMdcAdapter(MDCAdapter adapter) throws NoSuchFieldException, IllegalAccessException {
        Class<?> mdcClass = MDC.class;
        Field mdcAdapterField = mdcClass.getDeclaredField("mdcAdapter");
        mdcAdapterField.setAccessible(true);
        mdcAdapterField.set(null, adapter);
    }
}
