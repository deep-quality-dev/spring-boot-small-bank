package com.palm.bank.event;

import com.palm.bank.component.EtherWatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApplicationEvent implements ApplicationListener<ContextRefreshedEvent> {

    private final EtherWatcher etherWatcher;

    public ApplicationEvent(EtherWatcher etherWatcher) {
        this.etherWatcher = etherWatcher;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("onApplicationEvent" + event.toString());

        try {
            etherWatcher.setCurrentBlockNumber(etherWatcher.getBlockNumber());
            new Thread(etherWatcher).start();
        } catch (Exception ex) {
            log.error(ex.toString());
        }
    }
}
