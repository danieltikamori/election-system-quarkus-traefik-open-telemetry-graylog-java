package domain;

import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class CandidateServiceTest {

    @Inject
    CandidateService candidateService;

    @InjectMock
    CandidateRepository repository;

    @Test
    void save() {
        Candidate candidate = Instancio.create(Candidate.class);
        candidateService.save(candidate);

        verify(repository).save(candidate);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findAll() {
        List<Candidate> candidates = Instancio.stream(Candidate.class).limit(10).toList();

        when(repository.findAll()).thenReturn(candidates);

        List<Candidate> result = candidateService.findAll();

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);

        assertEquals(candidates, result);
    }

    @Test
    void findById_whenCandidateIsFound_returnsCandidate() {
        var domain = Instancio.create(Candidate.class);

        when(repository.findById(domain.id())).thenReturn(Optional.of(domain));

        var result = candidateService.findById(domain.id());

        verify(repository).findById(domain.id());
        verifyNoMoreInteractions(repository);

        assertEquals(result, domain);
    }

    @Test
    void findById_whenCandidateIsNotFound_throwsException() {
        var id = UUID.randomUUID().toString();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> candidateService.findById(id));
        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
    }
}