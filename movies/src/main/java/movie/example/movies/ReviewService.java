package movie.example.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MongoTemplate mongoTemplate; // Injecting MongoTemplate

    public Review createReview(String reviewBody, String imdbId) {
        Review review = reviewRepository.insert(new Review(reviewBody, LocalDateTime.now(), LocalDateTime.now()));
        System.out.println("Created Review: " + review);

        // Ensure the movie with the given imdbId exists
        var movie = mongoTemplate.findOne(new Query(Criteria.where("imdbId").is(imdbId)), Movie.class);
        if (movie == null) {
            System.out.println("Movie with imdbId " + imdbId + " not found.");
            return review;
        } else {
            System.out.println("Found Movie: " + movie);
        }

        // Perform the update operation
        var updateResult = mongoTemplate.update(Movie.class)
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("reviews", review))
                .first();

        System.out.println("Matched Count: " + updateResult.getMatchedCount());
        System.out.println("Modified Count: " + updateResult.getModifiedCount());

        // Retrieve the movie to verify the update
        movie = mongoTemplate.findOne(new Query(Criteria.where("imdbId").is(imdbId)), Movie.class);
        System.out.println("Updated Movie: " + movie);

        return review;
    }
}
