import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import '../styles/MovieList.css';

function MovieList() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    fetchMovies();
  }, [currentPage]);

  const fetchMovies = async () => {
    try {
      setLoading(true);
      const response = await api.get('/movies/popular', {
        params: { page: currentPage }
      });
      console.log(response.data)
      if (response.data && response.data.results) {
        setMovies(response.data.results);
        setTotalPages(response.data.total_pages);
      } else {
        setMovies([]);
        setTotalPages(1);
      }
      setLoading(false);
    } catch (err) {
      console.error('Ошибка при загрузке фильмов:', err);
      setError('Ошибка при загрузке фильмов');
      setLoading(false);
      setMovies([]);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      fetchMovies();
      return;
    }

    try {
      setLoading(true);
      const response = await api.get('/movies/search', {
        params: { query: searchQuery, page: currentPage }
      });
      if (response.data) {
        response.data.forEach(el => {
          el.id = el.tmdbId;
        });
        console.log(response.data)
        setMovies(response.data);
        setTotalPages(response.data || 1);
      } else {
        setMovies([]);
        setTotalPages(1);
      }
      setLoading(false);
    } catch (err) {
      console.error('Ошибка при поиске фильмов:', err);
      setError('Ошибка при поиске фильмов');
      setLoading(false);
      setMovies([]);
    }
  };

  if (loading) return <div>Загрузка...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div className="movie-list">
      <h1>Фильмы</h1>
      
      <form onSubmit={handleSearch} className="search-form">
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Поиск фильмов..."
          className="search-input"
        />
        <button type="submit" className="search-button">Поиск</button>
      </form>

      <div className="movie-grid">
        {movies && movies.length > 0 ? (
          movies.map((movie) => (
            <Link 
              to={`/movies/${movie.id}`}
              key={movie.id}
              className="movie-card"
            >
              <img 
                src={movie.posterUrl ? `${movie.posterUrl}` : '/placeholder.jpg'}
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
          ))
        ) : (
          <div className="no-movies">Фильмы не найдены</div>
        )}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button
            onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
            disabled={currentPage === 1}
            className="pagination-button"
          >
            Предыдущая
          </button>
          <span className="pagination-info">Страница {currentPage} из {totalPages}</span>
          <button
            onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
            disabled={currentPage === totalPages}
            className="pagination-button"
          >
            Следующая
          </button>
        </div>
      )}
    </div>
  );
}

export default MovieList; 