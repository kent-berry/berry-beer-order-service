package com.berrybeerorderservice.statemachine.actions;

import com.berrybeerorderservice.domain.BeerOrder;
import com.berrybeerorderservice.domain.BeerOrderEventEnum;
import com.berrybeerorderservice.domain.BeerOrderStatusEnum;
import com.berrybeerorderservice.repositories.BeerOrderRepository;
import com.berrybeerorderservice.services.BeerOrderManagerImpl;
import com.berrybeerorderservice.web.mappers.BeerOrderMapper;
import com.brewery.model.events.ValidateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.berrybeerorderservice.config.JmsConfig.VALIDATE_ORDER_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String beerOrderId = (String) stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(beerOrderId));

        jmsTemplate.convertAndSend(VALIDATE_ORDER_QUEUE, ValidateOrderRequest.builder()
                .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                .build());

        log.debug("Sent Validation request to queue for order id: " + beerOrderId);
    }
}