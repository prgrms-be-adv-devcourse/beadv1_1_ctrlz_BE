package com.user.application.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.user.application.port.in.PendingEventUseCase;
import com.user.application.port.out.ExternalEventPersistentPort;
import com.user.application.port.out.OutboundEventPublisher;
import com.user.domain.event.UserSignedUpEvent;
import com.user.infrastructure.scheduler.configuration.vo.PendingEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PendingEventApplication implements PendingEventUseCase {

	@Value("${custom.cart.topic.command}")
	private String cartCommandTopic;

	private final OutboundEventPublisher outboundEventPublisher;
	private final ExternalEventPersistentPort externalEventPersistentPort;

	@Override
	public void publishPendingEvents() {
		List<PendingEvent> pendingEvents = externalEventPersistentPort.findPendingEvents();
		pendingEvents.forEach(pendingEvent -> {
				outboundEventPublisher.publish(
					cartCommandTopic,
					UserSignedUpEvent.from(
						pendingEvent.userId(),
						pendingEvent.eventType()
					)
				);
				externalEventPersistentPort.completePublish(pendingEvent.userId(), pendingEvent.eventType());
			}
		);
	}
}
