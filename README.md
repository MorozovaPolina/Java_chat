# Java_chat
Чат, реализованный на java при помощи SelectioKey и SocketChannel.

Создание javadoc

```
javadoc -d docs src/client/*.java src/server/*.java
```

Создание jar
Перейти в директорию src

Серверная часть

```
javac server/*.java
jar cvfe server.jar server.Server server/*.class
```

Клиентская часть
```
javac client/*.java
jar cvfe client.jar client.Client client/*.class
```


Для использования приложения необходимо запустить сервер и как минимум один клиент.

На клиенте можно ввести команды:
 - online - получение списка онлайн пользователей
 - messages - история 100 последих сообщений
 - quit - выход
 - upload - загрузить файл, параметр - имя файла, находящегося в той же директории, что и jar запущенного приложения
 - download - скачать файл, рсположенный на сервере (файлы на сервере хрянтся в подпапке server)
 - Если введенные пользователем текст не начинается с одной и  этих команд, то он будет расссмотрен как сообщение.
