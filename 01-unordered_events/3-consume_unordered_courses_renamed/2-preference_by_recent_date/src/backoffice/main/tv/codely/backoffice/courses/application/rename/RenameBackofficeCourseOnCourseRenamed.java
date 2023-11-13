package tv.codely.backoffice.courses.application.rename;

import org.springframework.context.event.EventListener;
import tv.codely.shared.domain.Service;
import tv.codely.shared.domain.bus.event.DomainEventSubscriber;
import tv.codely.shared.domain.course.CourseRenamedDomainEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@DomainEventSubscriber({CourseRenamedDomainEvent.class})
public final class RenameBackofficeCourseOnCourseRenamed {
	private static final Map<String, Instant> lastExecutionTimeMap = new ConcurrentHashMap<>();

	private final BackofficeCourseRenamer renamer;

	public RenameBackofficeCourseOnCourseRenamed(BackofficeCourseRenamer renamer) {
		this.renamer = renamer;
	}

	@EventListener
	public void on(CourseRenamedDomainEvent event) {
		Instant eventTime = LocalDateTime.parse(
			event.occurredOn(),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		).atZone(ZoneId.systemDefault()).toInstant();

		if (eventDateIsMoreRecentThanLastTime(event.aggregateId(), eventTime)) {
			renamer.rename(event.aggregateId(), event.name());

			lastExecutionTimeMap.put(event.aggregateId(), eventTime);
		}
	}

	private boolean eventDateIsMoreRecentThanLastTime(String aggregateId, Instant eventTime) {
		return lastExecutionTimeMap.getOrDefault(aggregateId, Instant.MIN).isBefore(eventTime);
	}
}
