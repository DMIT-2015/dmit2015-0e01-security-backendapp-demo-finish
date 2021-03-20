package dmit2015.repository;

import dmit2015.entity.Movie;
import dmit2015.security.MovieSecurityInterceptor;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.security.enterprise.SecurityContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
//@Stateless
@Transactional
@Interceptors({MovieSecurityInterceptor.class})
public class MovieRepository {

    @Inject
    private SecurityContext _securityContext;

    @PersistenceContext//(unitName = "h2database-jpa-pu")
    private EntityManager em;

//    @RolesAllowed({"USER","Sales"})
    public void add(Movie newMovie) {
        String username = _securityContext.getCallerPrincipal().getName();
        newMovie.setUsername(username);

        em.persist(newMovie);
    }

    public void update(Movie updatedMovie) {
        Optional<Movie> optionalMovie = findById(updatedMovie.getId());
        if (optionalMovie.isPresent()) {
            Movie existingMovie = optionalMovie.get();
            existingMovie.setTitle(updatedMovie.getTitle());
            existingMovie.setGenre(updatedMovie.getGenre());
            existingMovie.setPrice(updatedMovie.getPrice());
            existingMovie.setRating(updatedMovie.getRating());
            existingMovie.setReleaseDate(updatedMovie.getReleaseDate());
            em.merge(existingMovie);
            em.flush();
        }
    }

    protected void remove(Movie existingMovie) {
        em.remove(em.merge(existingMovie));
        em.flush();
    }

    public void remove(Long movieID) {
        Optional<Movie> optionalMovie = findById(movieID);
        if (optionalMovie.isPresent()) {
            Movie existingMovie = optionalMovie.get();
            remove(existingMovie);
        }
    }

    public Optional<Movie> findById(Long movieID) {
        Optional<Movie> optionalMovie = Optional.empty();
        try {
            Movie querySingleResult = em.find(Movie.class, movieID);
            if (querySingleResult != null) {
                optionalMovie = Optional.of(querySingleResult);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return optionalMovie;
    }

    public List<Movie> findAll() {
        List<Movie> queryResultList = null;
        if (_securityContext.isCallerInRole("USER") || _securityContext.isCallerInRole("Sales")) {
            // For the roles USER and Sales return only Movies associated with the current user
            String username = _securityContext.getCallerPrincipal().getName();
            queryResultList =  em.createQuery(
                    "SELECT m FROM Movie m WHERE m.username = :usernameValue"
                    , Movie.class)
                    .setParameter("usernameValue", username)
                    .getResultList();
        } else {
            // For all other users return all movies
            queryResultList =  em.createQuery(
                    "SELECT m FROM Movie m "
                    , Movie.class)
                    .getResultList();
        }


        return queryResultList;
    }

    public List<Movie> findAllOrderByTitle() {
        return em.createQuery(
                "SELECT m FROM Movie m ORDER BY m.title"
                , Movie.class)
                .getResultList();
    }
}