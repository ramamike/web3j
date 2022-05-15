package com.learn.task.SpringBlockChainWebDB.contoller;

import com.learn.task.SpringBlockChainWebDB.service.ComparingMethods;
import com.learn.task.SpringBlockChainWebDB.entity.EventEntity;
import com.learn.task.SpringBlockChainWebDB.entity.StakingEntity;
import com.learn.task.SpringBlockChainWebDB.entity.StakingFromEventEntity;
import com.learn.task.SpringBlockChainWebDB.repository.EventRepository;
import com.learn.task.SpringBlockChainWebDB.repository.StakingFromEventRepository;
import com.learn.task.SpringBlockChainWebDB.repository.StakingRepository;
import com.learn.task.SpringBlockChainWebDB.service.EventService;
import com.learn.task.SpringBlockChainWebDB.service.StakingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@Slf4j
public class MainController {

    @Autowired
    private EventService eventService;

    @Autowired
    private StakingService stakingService;
    @Autowired
    public MainController(EventRepository eventRepository,
                          StakingFromEventRepository stakingEventRepository,
                          StakingRepository stakingRepository,
                          ComparingMethods comparing) {
        this.eventRepository = eventRepository;
        this.stakingEventRepository=stakingEventRepository;
        this.stakingRepository = stakingRepository;
        this.comparing=comparing;
    }

    private EventRepository eventRepository;

    private StakingFromEventRepository stakingEventRepository;
    private StakingRepository stakingRepository;

    private ComparingMethods comparing;


    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Главная страница");
        return "home";
    }

    @GetMapping("/events")
    public String events(Model model) {
        List<EventEntity> eventEntityList =eventRepository.findAll();
        model.addAttribute("eventList", eventEntityList);
        return "events";
    }

    @GetMapping("/stakings")
    public String stakings(Model model) {
        List<StakingEntity> stakingEntities =stakingRepository.findAll();
        model.addAttribute("stakingList", stakingEntities);
        return "stakings";
    }

    @GetMapping("/comparing")
    public  String comparing(Model model){
        List<StakingFromEventEntity> stakingEventEntities =stakingEventRepository.findAll();
        model.addAttribute("stakingEventList", stakingEventEntities);
        List<StakingEntity> stakingEntities =stakingRepository.findAll();
        model.addAttribute("stakingList", stakingEntities);


        String resultComparing="Waiting input data";

        if(eventService.getContactAddress()==null) {
            resultComparing="Waiting input data";
        } else if (comparing.doComparing()) {
            resultComparing="Last browse result: Methods are equal, Ok";
        } else if (!comparing.doComparing() ){
            resultComparing="Last browse result: Methods are not equal";
        }

        model.addAttribute("resultComparing", resultComparing);

        return "comparing";
    }

    @PostMapping("/comparing")
    public String blogPostAdd(@RequestParam String urlHttpServiceScan,
                              @RequestParam String contractAddressScan,
                              @RequestParam String urlHttpServiceRead,
                              @RequestParam String contractAddressRead,
                              Model model) {

        // initialing the end point and the contact address to compare
        eventService.setUrlHttpService(urlHttpServiceScan);
        eventService.setContactAddress(contractAddressScan);
        stakingService.setUrlHttpService(urlHttpServiceRead);
        stakingService.setContactAddress(contractAddressRead);

        // reset flags
        comparing.setComparingFinished(false);
        eventService.setCheckingFinished(false);
        eventService.setScanFinished(false);

        // preparing repositories
        eventRepository.deleteAll();
        stakingEventRepository.deleteAll();
        stakingRepository.deleteAll();

        // get stakings with two methods
            //scanning stakings from events require time

        stakingService.saveStakingListToDB();

        // waiting scan of events
        while(!eventService.isScanFinished() && !eventService.isCheckingFinished()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.info("Thread exception info", e);
            }
        }

        // Comparing
        comparing.doComparing();

        return "redirect:/comparing";
    }

    @PostMapping("/clear")
    public String clearComparingData(Model model){
        eventRepository.deleteAll();
        stakingEventRepository.deleteAll();
        stakingRepository.deleteAll();
        return "redirect:/comparing";
    }

}
