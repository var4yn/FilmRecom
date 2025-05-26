import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import RatingForm from '../components/Movie/RatingForm';
import authService from '../services/auth';

function MovieDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [movie, setMovie] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userRating, setUserRating] = useState(null);

  useEffect(() => {
    const fetchMovieData = async () => {
      try {
        let movieData = await api.getMovieById(id);
        let recommendationsData = await api.getMovieRecommendations(id);
        console.log('Movie data:', movieData);
        console.log('Recommendations:', recommendationsData);
        setMovie(movieData);
        setRecommendations(recommendationsData);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching movie data:', err);
        setError('Ошибка при загрузке данных фильма');
        setLoading(false);
      }
    };

    fetchMovieData();
  }, [id]);

  const handleRatingSubmit = async (rating) => {
    setUserRating(rating);
    // Обновляем рекомендации после оценки
    try {
      const recommendationsData = await api.getMovieRecommendations(id);
      setRecommendations(recommendationsData);
    } catch (err) {
      console.error('Ошибка при обновлении рекомендаций:', err);
    }
  };

  if (loading) return <div>Загрузка...</div>;
  if (error) return <div>{error}</div>;
  if (!movie) return <div>Фильм не найден</div>;

  const isAuthenticated = authService.isAuthenticated();

  return (
    <div className="movie-detail">
      <div className="movie-detail-header">
        <img src={movie.posterUrl} alt={movie.title} />
        <div className="movie-detail-info">
          <h1>{movie.title}</h1>
          <p><strong>Год:</strong> {movie.releaseYear}</p>
          <p><strong>Описание:</strong> {movie.overview}</p>
          {isAuthenticated && (
            <RatingForm 
              movieId={movie.tmdbId} 
              onRatingSubmit={handleRatingSubmit}
            />
          )}
          {!isAuthenticated && (
            <p className="login-prompt">
              <a href="/login">Войдите</a>, чтобы оценить фильм
            </p>
          )}
        </div>
      </div>

      <div className="recommendations">
        <h2>Рекомендуемые фильмы</h2>
        <div className="movie-grid">
          {recommendations.map((rec) => (
            <Link 
              key={rec.tmdbId} 
              to={`/movies/${rec.tmdbId}`}
              className="movie-card"
            >
              <img src={rec.posterUrl} alt={rec.title} />
              <div className="movie-card-content">
                <h3>{rec.title}</h3>
                <p>{rec.releaseYear}</p>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}

export default MovieDetail; 