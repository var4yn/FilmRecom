import api from './api';

const getMovies = async () => {
  try {
    console.log('Отправка запроса на получение фильмов');
    const response = await api.get('/movies');
    return response.data;
  } catch (error) {
    console.error('Error fetching movies:', error);
    throw error;
  }
};

const getMovieById = async (id) => {
  try {
    const response = await api.get(`/movies/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching movie ${id}:`, error);
    throw error;
  }
};

const getMovieRecommendations = async (id) => {
  try {
    const response = await api.get(`/movies/${id}/recommendations`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching recommendations for movie ${id}:`, error);
    throw error;
  }
};

const getUserRecommendations = async () => {
  try {
    const response = await api.get('/recommendations');
    return response.data;
  } catch (error) {
    console.error('Error fetching user recommendations:', error);
    throw error;
  }
};

export default {
  getMovies,
  getMovieById,
  getMovieRecommendations,
  getUserRecommendations
}; 