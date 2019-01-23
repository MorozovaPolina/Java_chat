# Java_chat
Чат, реализованный Анастасией Чернышевой и Морозовой Полиной на java при помощи SelectioKey и SocketChannel.

Для использования приложения необходимо запустить сервер и как минимум один клиент.

На клиенте можно ввести команды:
 - online - получение списка онлайн пользователей
 - messages - история 100 последих сообщений
 - quit - выход
 - upload - загрузить файл, параметр - имя файла, находящегося в той же директории, что и jar запущенного приложения
 - download - скачать файл, расположенный на сервере (файлы на сервере хрянтся в подпапке server)
 - Если введенные пользователем текст не начинается с одной и  этих команд, то он будет расссмотрен как сообщение.

Jar клиентской и серверной части (client.jar и server.jar соответственно) находятся в папке Chat. 
JavaDoc можно найти в папке Chat/doc.
Исходный код приложения находится в папке Chat/src.

Ниже представлены скрипты создания документации и jar.

Создание javadoc

```
javadoc -d docs src/client/*.java src/server/*.java src/common/*.java
```

Создание jar
Перейти в директорию src

Серверная часть

```
javac server/*.java
javac common/*.java
jar cvfe server.jar server.Server server/*.class common/*.class
```

Клиентская часть
```
javac client/*.java
javac common/*.java
jar cvfe client.jar client.Client client/*.class common/*.class
```
