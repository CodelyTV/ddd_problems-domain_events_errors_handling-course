package tv.codely.backoffice.courses.infrastructure.persistence;

import org.springframework.context.annotation.Primary;
import tv.codely.backoffice.courses.domain.BackofficeCourse;
import tv.codely.backoffice.courses.domain.BackofficeCourseRepository;
import tv.codely.shared.domain.Service;
import tv.codely.shared.domain.criteria.Criteria;
import tv.codely.shared.infrastructure.elasticsearch.ElasticsearchClient;
import tv.codely.shared.infrastructure.elasticsearch.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

@Primary
@Service
public final class ElasticsearchBackofficeCourseRepository extends ElasticsearchRepository<BackofficeCourse> implements BackofficeCourseRepository {
	private static boolean searchExecuted = false;

	public ElasticsearchBackofficeCourseRepository(ElasticsearchClient client) {
		super(client);
	}

	@Override
	public void save(BackofficeCourse course) {
		var hasSearchBeenExecuted = searchExecuted;

		System.out.println("Has been searched: " + hasSearchBeenExecuted);

		var existingCourse = this.searchById(course.id(), BackofficeCourse::fromPrimitives);

		if (existingCourse.isPresent()) {
			if (!hasSearchBeenExecuted) {
				throw new RuntimeException("Duplicate key for course: " + course.id());
			}

			System.out.println("Updating course");
			persist(course.id(), course.toPrimitives());
		} else {
			System.out.println("Creating new course");

			persist(course.id(), course.toPrimitives());
		}

		searchExecuted = false;
	}

	@Override
	public Optional<BackofficeCourse> search(String id) {
		searchExecuted = true;

		return this.searchById(id, BackofficeCourse::fromPrimitives);
	}

	@Override
	public List<BackofficeCourse> searchAll() {
		return searchAllInElastic(BackofficeCourse::fromPrimitives);
	}

	@Override
	public List<BackofficeCourse> matching(Criteria criteria) {
		return searchByCriteria(criteria, BackofficeCourse::fromPrimitives);
	}

	@Override
	protected String moduleName() {
		return "courses";
	}
}
