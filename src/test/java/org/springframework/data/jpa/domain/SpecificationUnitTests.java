/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.domain;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.jpa.domain.Specification.*;
import static org.springframework.data.jpa.domain.Specification.not;
import static org.springframework.util.SerializationUtils.*;

import java.io.Serializable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link Specification}.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Sebastian Staudt
 * @author Jens Schauder
 * @author Daniel Shuy
 */
@SuppressWarnings("serial")
@RunWith(MockitoJUnitRunner.class)
public class SpecificationUnitTests implements Serializable {

	Specification<Object> spec;
	@Mock(extraInterfaces = Serializable.class) Root<Object> root;
	@Mock(extraInterfaces = Serializable.class) CriteriaQuery<?> query;
	@Mock(extraInterfaces = Serializable.class) CriteriaBuilder builder;

	@Mock(extraInterfaces = Serializable.class) Predicate predicate;

	@Before
	public void setUp() {

		spec = (root, query, cb) -> predicate;
	}

	@Test // DATAJPA-300, DATAJPA-1170
	public void createsSpecificationsFromNull() {

		Specification<Object> specification = where(null);
		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isNull();
	}

	@Test // DATAJPA-300, DATAJPA-1170
	public void negatesNullSpecToNull() {

		Specification<Object> specification = not(null);

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isNull();
	}

	@Test // DATAJPA-300, DATAJPA-1170
	public void andConcatenatesSpecToNullSpec() {

		Specification<Object> specification = where(null);
		specification = specification.and(spec);

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isEqualTo(predicate);
	}

	@Test // DATAJPA-300, DATAJPA-1170
	public void andConcatenatesNullSpecToSpec() {

		Specification<Object> specification = spec.and(null);

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isEqualTo(predicate);
	}

	@Test // DATAJPA-300, DATAJPA-1170
	public void orConcatenatesSpecToNullSpec() {

		Specification<Object> specification = where(null);
		specification = specification.or(spec);

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isEqualTo(predicate);
	}

	@Test // DATAJPA-300, DATAJPA-1170
	public void orConcatenatesNullSpecToSpec() {

		Specification<Object> specification = spec.or(null);

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isEqualTo(predicate);
	}

	@Test // DATAJPA-1651
	public void allOfConcatenatesNull() {

		Specification<Object> specification = Specification.allOf(null, spec, null);

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isEqualTo(predicate);
	}

	@Test // DATAJPA-1651
	public void anyOfConcatenatesNull() {

		Specification<Object> specification = Specification.anyOf(null, spec, null);

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isEqualTo(predicate);
	}

	@Test // DATAJPA-1651
	public void emptyAllOfReturnsEmptySpecification() {

		Specification<Object> specification = Specification.allOf();

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isNull();
	}

	@Test // DATAJPA-1651
	public void emptyAnyOfReturnsEmptySpecification() {

		Specification<Object> specification = Specification.anyOf();

		assertThat(specification).isNotNull();
		assertThat(specification.toPredicate(root, query, builder)).isNull();
	}

	@Test // DATAJPA-523
	public void specificationsShouldBeSerializable() {

		Specification<Object> serializableSpec = new SerializableSpecification();
		Specification<Object> specification = serializableSpec.and(serializableSpec);

		assertThat(specification).isNotNull();

		@SuppressWarnings("unchecked")
		Specification<Object> transferredSpecification = (Specification<Object>) deserialize(serialize(specification));

		assertThat(transferredSpecification).isNotNull();
	}

	@Test // DATAJPA-523
	public void complexSpecificationsShouldBeSerializable() {

		SerializableSpecification serializableSpec = new SerializableSpecification();
		Specification<Object> specification = Specification
				.not(serializableSpec.and(serializableSpec).or(serializableSpec));

		assertThat(specification).isNotNull();

		@SuppressWarnings("unchecked")
		Specification<Object> transferredSpecification = (Specification<Object>) deserialize(serialize(specification));

		assertThat(transferredSpecification).isNotNull();
	}

	public class SerializableSpecification implements Serializable, Specification<Object> {

		@Override
		public Predicate toPredicate(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
			return null;
		}
	}
}
