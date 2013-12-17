Limb docs searcher
==================

Поисковик документации по репозитория Limb
-------------------------------------------------

**Реализация**

    * java
    * Postgresql

**Функциональность**

    * Интерфейс пользователя
    * Поиск по ключевым словам md файлов
    * Json API
    * Поиск по ключевым словам md файлов

**Рабочее окружение**
          
Разработка ведется под ОС Ubuntu 12.04, для корректной работы окружения необходимо поднять следующие сервисы:

    * `postgresql 8.4` (рабочая база данных)
    * `sphinxsearch 2.0` (поисковый движок)

**Настройка development окружения**

1. Получаем основной репозиторий проекта

    $ git clone git@gitlab.eastwoodlab.ru:limb-docs-searcher.git
2. Устанавливаем sbt

    $ wget http://repo.scala-sbt.org/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.13.0/sbt.deb
    $ dpkg -i sbt.deb
3. Накатываем на базу иницилазационные скрипты из папки `init` сначала `bootstrap.sql`, затем `data.sql`
4. Запускаем Sphinx c конфигом
5. Исправляем настройки в файле conf/application.conf (Параметры подключения к БД, параметры подключения к Sphinx)
6. Запускем development-сервер
   $ sbt run

**Настройка production окружения**

1. В репозитории текущий работающий релиз лежит в ветке `master` 
2. Перед сборкой пакета нужно установить все сборочные зависимости приложения:
   * sbt
   * default-jdk
3. Подготовить базу данных, применить инициализационные скрипты
   * `init/bootstrap.sql`
   * `init/data.sql`
4. Собрать deb-пакет

   $ sbt debian:package-bin
 В результате получим пакет limb-docs-searcher
5. Установить deb пакет в систему

   $ gdebi target/limb-docs-searcher_<версия>.deb
5. Настройки приложения находятся в файле `/etc/limb-docs-searcher/application.conf`
6. Настроить подключение к базе данных (параметры db.default.*)

**Управление сервером**

* Запуск
  $ start limb-docs-searcher

* Остановка

  $ stop limb-docs-searcher
* Перезапуск

  $ restart limb-docs-searcher

**Использование**

1. /search, помимо параметра s (ключевые слова) имеет параметры limit и offset для указания параметров вывода.
limit указывает сколько найдённый страниц выводить, offset - начиная с какой.
2. /search.json имеет такие же параметры, что и /search, отличие только в том, что выводит в формате json.

**Дополнительно**

* Индесация ручками

$ curl -X POST http://0.0.0.0:9000/update?token=<токен>
После заполнения базы необходимо сделать (факт заполнения базы можно узнать по логам)
$ indexer --all --rotate --config <путь к конфигу>/sphinx.conf

**Параметр конфигурации**
secret_token_for_indexing - токен авторизации
sphinxServer = адрес сервера на котором крутится Sphinx
sphinxPort = порт, на котором Sphinx висит
download_limb_if_not_exists = клонировать ли limb, если его нет
limb_git_path = адрес удалённого git репозитория
limb_local_path = куда класть репозиторий на локальной машине
update_limb_local_repo = обновлять ли локальный репозиторий если есть такая возможность
