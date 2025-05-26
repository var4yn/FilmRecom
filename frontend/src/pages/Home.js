import React from 'react';
import { Link } from 'react-router-dom';

function Home() {
  return (
    <div className="home">
      <h1>Добро пожаловать в FilmRecom</h1>
      <p>Ваш персональный помощник в выборе фильмов</p>
      <Link to="/movies" className="btn">
        Смотреть фильмы
      </Link>
    </div>
  );
}

export default Home; 