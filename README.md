# FilmRecom
Учебный проект по созданию рекомендательного сервиса для фильмов и сериалов.

## Описание проекта
FilmRecom — это веб-приложение, предназначенное для ведения учета просмотренных фильмов и сериалов, выставления оценок, создания персональных подборок, а также получения рекомендаций на основе пользовательских предпочтений.

## Функциональные возможности
- Добавление просмотренных фильмов и выставление оценок.
- Фильтрация фильмов по жанрам и году выпуска.
- Получение персонализированных рекомендаций на основе оценок и предпочтений.
- Поиск фильмов через внешние API.
- Личный кабинет пользователя.
- Статистика для администраторов:
    - Популярные фильмы за всё время и за определённый период.
    - Популярные жанры за всё время и за определённый период.

## План реализации (техническое задание)

Backend
1. Изучение внешнего API для работы с фильмами (например, IMDb API) и знакомство с его сущностями.
2. Проектирование и реализация схемы базы данных (в основе — пользовательские оценки).
3. Создание класса-обёртки для работы со сторонним API (поиск и получение информации о фильмах).
4. Реализация CRUD-операций для работы с сущностями: пользователи, фильмы, подборки.
5. Разработка алгоритма рекомендаций на основе любимых жанров пользователя.
6. Реализация алгоритма косинусного сходства для оценки схожести предпочтений.

Frontend
1. Главная страница:
    - Поиск фильмов.
    - Кнопка для перехода к рекомендательному модулю.
2. Личный кабинет пользователя:
    - Просмотр и редактирование просмотренных фильмов.
    - Управление подборками.
    - История оценок.

Для администратора
- Доступ к аналитике:
    - Топ популярных фильмов за:
        - всё время,
        - 2–4 недели,
        - 3/6 месяцев.
    - Аналогичная аналитика по жанрам.

## Ожидаемый минимум MVP
- [ ] Возможность добавления просмотренных фильмов с пользовательскими оценками.
- [ ] Фильтрация фильмов по жанрам и годам.
- [ ] Генерация рекомендаций на основе пользовательских данных.