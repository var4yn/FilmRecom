import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import authService from "../services/auth";

function Recommendations() {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchRecommendations = async () => {
      try {
        const user = await authService.getCurrentUser();
        if (!user) {
          setError('Требуется авторизация для просмотра рекомендаций');
          setLoading(false);
          return;
        }
        const data = await api.getUserRecommendations(user.id);
        setRecommendations(data);
        setLoading(false);
      } catch (err) {
        console.error('Ошибка при загрузке рекомендаций:', err);
        setError(err.message || 'Ошибка при загрузке рекомендаций');
        setLoading(false);
      }
    };

    fetchRecommendations();
  }, []);

  if (loading) return <div>Загрузка...</div>;

  return (
    <div className="recommendations-page">
      <h1>Рекомендации для вас</h1>
      
      {error ? (
        <div className="error-message">
          <p>{error}</p>
          {error.includes('авторизация') && (
            <Link to="/login" className="btn">Войти</Link>
          )}
        </div>
      ) : (
        <>
          <p className="recommendations-description">
            На основе ваших оценок и предпочтений мы подобрали для вас следующие фильмы
          </p>

          {recommendations.length > 0 ? (
            <div className="movie-grid">
              {recommendations.map((movie) => (
                <Link 
                  to={`/movies/${movie.tmdbId}`}
                  key={movie.tmdbId}
                  className="movie-card"
                >
                  <img 
                    src={movie.posterUrl} 
                    alt={movie.title} 
                    className="movie-poster"
                  />
                  <div className="movie-card-content">
                    <h3 className="movie-title">{movie.title}</h3>
                    <p className="movie-year">
                      {movie.releaseYear || (movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : 'Н/Д')}
                    </p>
                    <p className="movie-rating">
                      Рейтинг: {movie.voteAverage ? movie.voteAverage.toFixed(1) : 'Н/Д'}
                    </p>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="no-recommendations">
              <p>У вас пока нет рекомендаций</p>
              <p>Оцените несколько фильмов, чтобы получить персонализированные рекомендации</p>
              <Link to="/movies" className="btn">Перейти к фильмам</Link>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default Recommendations; 