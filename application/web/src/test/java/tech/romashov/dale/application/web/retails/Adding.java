package tech.romashov.dale.application.web.retails;

import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Before;
import org.junit.Test;
import tech.romashov.dale.application.web.properties.SystemPropertyEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class Adding extends RetailsTests {
    @Before
    public void before() {
        SystemPropertyEntity limit = propsRepository.findByKey("limit");
        limit.value = "2";
        propsRepository.save(limit);
        retailsRepository.deleteAll();
    }

    @Test
    public void addNew_sizeLowThanLimit_shouldBeAdded() throws RetailException {
        // Arrange
        addDummyBusyRetail("1.1.1.1");

        // Act
        retailService.add(Vendors.ALL, "2.2.2.2");

        // Assert
        assertThat(retailsRepository.findAll(), new IsIterableWithSize<>(equalTo(2)));
    }

    @Test(expected = RetailException.class)
    public void addNew_allExistentShouldBeBusy_shouldNotBeAdded() throws RetailException {
        // Arrange
        addDummyBusyRetail("1.1.1.1");
        addDummyBusyRetail("2.2.2.2");

        // Act
        retailService.add(Vendors.ALL, "3.3.3.3");
    }

    @Test
    public void addNew_oneOfExistentShouldBeFree_shouldBeAdded() throws RetailException {
        // Arrange
        RetailEntity busy = addDummyBusyRetail("1.1.1.1");
        RetailEntity free = addDummyFreeRetail("2.2.2.2");

        // Act
        RetailEntity result = retailService.add(Vendors.ALL, "3.3.3.3");

        // Assert
        List<RetailEntity> findAll = StreamSupport.stream(retailsRepository.findAll().spliterator(), false).collect(Collectors.toList());
        assertThat(findAll, hasSize(2));
        assertThat(findAll.stream().filter(r -> r.ip.equalsIgnoreCase(free.ip)).count(), equalTo(0L));
        assertThat(findAll.stream().filter(r -> r.ip.equalsIgnoreCase(result.ip)).count(), equalTo(1L));
    }
}
