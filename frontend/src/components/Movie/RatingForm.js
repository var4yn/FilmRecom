import React, { useState } from 'react';
import api from '../../services/api';

function RatingForm({ movieId, onRatingSubmit }) {
  const [rating, setRating] = useState(0);
  const [hover, setHover] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) {
      setError('Пожалуйста, выберите оценку');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await api.submitRating(movieId, rating);
      if (onRatingSubmit) {
        onRatingSubmit(rating);
      }
    } catch (err) {
      setError('Ошибка при сохранении оценки');
      console.error('Ошибка при сохранении оценки:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rating-form">
      <h3>Оцените фильм</h3>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="star-rating">
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              key={star}
              type="button"
              className={`star ${star <= (hover || rating) ? 'active' : ''}`}
              onClick={() => setRating(star)}
              onMouseEnter={() => setHover(star)}
              onMouseLeave={() => setHover(0)}
            >
              ★
            </button>
          ))}
        </div>
        <button 
          type="submit" 
          className="btn btn-primary"
          disabled={loading}
        >
          {loading ? 'Сохранение...' : 'Сохранить оценку'}
        </button>
      </form>
    </div>
  );
}

export default RatingForm; 