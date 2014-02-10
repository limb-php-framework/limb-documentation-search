Limb docs searcher
==================

Поисковик документации по репозиторию Limb
-------------------------------------------------

**Реализация**

    * Scala
    * Postgresql

**Функциональность**

    * Интерфейс пользователя
    * Поиск md файлов по ключевым словам
    * Json API

**Рабочее окружение**

Разработка ведется под ОС Ubuntu 12.04, для корректной работы окружения необходимо поднять следующие сервисы:

    * `postgresql 9.1` (рабочая база данных)
    * `sphinxsearch 2.0` (поисковый движок)

**Настройка development окружения**

1. Получаем основной репозиторий проекта

    ``git clone https://github.com/knevcher/limb-documentation-search.git``
2. Устанавливаем sbt

    ```bash
    wget http://repo.scala-sbt.org/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.13.0/sbt.deb
    gdebi sbt.deb
    ```

3. Накатываем на базу иницилазационный скрипт из папки `init` `bootstrap.sql`
4. Запускаем searchd c конфигом (не забываем отредактировать его, параметры БД поменять например), который в conf/sphinx.conf. Если хотите сразу же заполнить базу и сфинкс данными - посмотрите на пункт "Индексация ручками" в "Дополнительно".

    `$ searchd -c conf/sphinx.conf`
5. Копируем conf/application.dev.conf.ex в conf/application.dev.conf и настраиваем в нём всё что нужно (параметры подключения к БД, параметры подключения к Sphinx)
6. Запускем development-сервер

  `$ sbt run`

**Настройка production окружения**

Приложение заточено под Ubuntu 12.04.

1. В репозитории текущий работающий релиз лежит в ветке `master`
2. Перед сборкой пакета нужно установить все сборочные зависимости приложения:

   * default-jdk
   * sbt (http://repo.scala-sbt.org/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.13.0/sbt.deb)
   * dh-make
3. Подготовить базу данных, применить инициализационный скрипт
   `init/bootstrap.sql`
4. Собрать deb-пакет

   `debuild -us -uc`

    В результате получим пакет limb-docs-searcher
5. Установить deb пакет в систему

   `gdebi ../limb-docs-searcher_<версия>_all.deb`
6. Установить deb пакет со статикой

   `gdebi ../limb-docs-searcher-static_<версия>_all.deb`
7. Настройки приложения находятся в файле `/etc/limb-docs-searcher/application.conf`
8. Настроить подключение к базе данных (параметры db.default.*)
9. Запускаем приложение

   `sudo start limb-docs-searcher`

    Если

    `status limb-docs-searcher` после запуска отрицательный, ищем причину в логах upstart: /var/log/upstart/limb-docs-searcher.log

10. Запускаем индексацию запросом

   ``` $ curl --data "token=`cat /etc/limb-docs-searcher/application.conf|grep token|awk '{print $3}'|sed 's/"//g'`" http://localhost:`cat /etc/default/limb-docs-searcher|grep PORT|sed 's/PORT=//g'`/update -X POST```

    Запускать эту команду надо на той же машине где установлен поисковик.
11. Демон сфинкса необходимо запускать после того, как приложение проиндексирует limb репозиторий, в противном случае он просто не запустится по причине отсутствия индексных файлов. Факт того, что он проиндексировал можно узнать по логам если стоит уровень INFO. Если не стоит - попробуйте подождать минут 10.
   Запустить searchd (демон сфинкса) (не забываем отредактировать его, параметры БД поменять например)

   `searchd -c /etc/limb-docs-searcher/sphinx.conf`

    После изменения конфига /etc/limb-docs-searcher/sphinx.conf необходимо перезапускать searchd.
12. Настроить nginx для раздачи статики (конфиг nginx-static.conf.ex прилагается)

**Управление сервером**

* Запуск

    `start limb-docs-searcher`

* Остановка

  `stop limb-docs-searcher`
* Перезапуск

  `restart limb-docs-searcher`

**Настройка nginx**

1. Для сервера с приложением берём конфиг nginx.conf.ex
2. Для сервера со статикой nginx-static.conf.ex
3. При необходимости редактируем

**Использование**

1. Для поиска надо ввести ключевые слова в единственное поле на главной странице или на странице /search.
2. /search.json так же как и /search имеет параметр page.

**Страница /status**

Статусная страница отображает состояние используемых сервисов (Postgres, Sphinx), версию deb-пакета, дату сборки и название.

**Настройка логов**

В приложении используется библиотека logback, подробная документация есть там http://logback.qos.ch/manual/introduction.html.

Инструкция:

Для обработки поступающих от приложения логов нужно создать в конфиге logback logger с соответствующим названием (параметр name). Параметр level означает минимальную важность ошибки, которая будет обрабатываться. Пример:

```xml
<logger name="play" level="ERROR" />
```

Appender - блок, который позволяет настраивать куда писать логи. Это может быть файл, стандартный вывод, syslog, email, etc. Примеры:

```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>/var/log/limb-docs-searcher/limb-docs-searcher.log</file>
    <encoder>
        <pattern>%date - [%level] - from %logger in %thread %n%message%n%xException%n</pattern>
    </encoder>
</appender>
```

```xml
<appender name="SYSLOG" class="ch.qos.logback.classic.net.SyslogAppender">
    <syslogHost>localhost</syslogHost>
    <facility>USER</facility>
    <port>514</port>
    <suffixPattern>Limb docs searcher[%thread] %logger %msg</suffixPattern>
</appender>
```

Подробно об appender http://logback.qos.ch/manual/appenders.html
В блоке appender можно указывать параметр filter, который будет фильтровать логи например по степени важности. Примеры:

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <filter class="ch.qos.logback.classic.filter.LevelFilter">
        <level>INFO</level>
    </filter>
    <encoder>
         <pattern>%-4relative [%thread] %-5level %logger{30} - %msg%n</pattern>
    </encoder>
</appender>
```
Этот пример будет пропускать логи только с уровнем важности INFO.

```xml
<filter class="ch.qos.logback.core.filter.EvaluatorFilter">
    <evaluator class="ch.qos.logback.classic.boolex.GEventEvaluator">
        <expression>
            e.level.toInt() >= WARN.toInt()
            &amp;&amp;  <!-- Stands for && in XML -->
            !(e.mdc?.get("req.userAgent") ~= /Googlebot|msnbot|Yahoo/ )
        </expression>
    </evaluator>
</filter>
```
Подробно о фильтрах http://logback.qos.ch/manual/filters.html

Непосредственно связать logger с appender можно так:
```xml
<logger name="application" level="ERROR">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="FILE" />
</logger>
```

**Дополнительно**

* Индексация ручками

`$ curl -X POST http://<домен>/update?token=<токен>`

Токен находится в application.conf. Выдернуть его и отправить запрос можно так (при условии что deb-пакет установлен):

``` $ curl --data "token=`cat /etc/limb-docs-searcher/application.conf|grep token|awk '{print $3}'|sed 's/"//g'`" http://localhost:`cat /etc/default/limb-docs-searcher|grep PORT|sed 's/PORT=//g'`/update -X POST```

После заполнения базы необходимо сделать (факт заполнения базы можно узнать по логам)

`$ indexer --all --rotate --config <путь к конфигу>/sphinx.conf`

**Параметры конфигурации**
* from_user - от какого пользователя приложение будет работать
* secret_token_for_indexing - токен авторизации
* sphinx_server - адрес сервера на котором крутится Sphinx
* sphinx_port - порт, на котором Sphinx висит
* download_limb_if_not_exists - клонировать ли limb, если его нет
* limb_git_path - адрес удалённого git репозитория
* limb_local_path - куда класть репозиторий на локальной машине
* update_limb_local_repo - обновлять ли локальный репозиторий если есть такая возможность
* last_modified_file - путь к файлу, в котором хранится дата последней индексации
* page_results - количество результатов на страницу
* index_name - название индекса Sphinx
* count_snippets - количество слов кокруг ключевого в описании
* static_url - адрес для статики
* dbplugin=disabled нужно для отключения стандартного DBPlugin
* evolutionplugin=disabled для отключения Evolution
* future_timeout - время ожидание ответа от сфинкса

настройки блока play > akka > actor > default-dispatcher > fork-join-executor
* parallelism-min - минимальное количество тредов
* parallelism-max - максимальное количество тредов

настройки блока akka.actor.deployment которые можно трогать:
* router - тип маршрутизации сообщений по акторам
* nr-of-instances - колиество выделенных для работы со сфинксом акторов

**Возможные ошибки и их устранение**

* AskTimeoutException: Timed out - возникает если значение параметра future_timeout слишком мало по сравнению с необходимым временем для запроса к Сфинксу под текущей нагрузкой
