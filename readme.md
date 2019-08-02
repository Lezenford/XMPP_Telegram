<h1>Telegram-бот как xmpp-клиент</h1>

<h2>О боте</h2>
Бот предназначен для подключения xmpp-аккаунтов (jabber) и их эксплуатации с использованием в качестве клиента Telegram.
Приложение реализовано как standalone, содержит в себе готовый к работе web-сервер и для своего запуска требует только корректную версию jdk.
Для конфигурации служит файл `external.properties`, расположенный в каталоге запуска jar. 

<h2>Настройка https</h2>
Для корректной работы в режиме webhook Телеграм требуется наличие валидного сертификата на стороне бота.  
Это может быть сертификат, выданный удостоверяющим центром, либо самоподписанный сертификат.  
Следующие параметры в `external.properties` отвечают за управление сертификатами:  

`server.port=` - порт сервера. Для Телеграм разрешено только использование портов 433, 8443, 80, 88    
`telegram.cert=` - путь (относительный или абсолтный) до публичного ключа в формате .pem.   
`server.ssl.key-alias=` - alias для ключей в keystore      
`server.ssl.key-store-type=` - тип хранилища. рекомендуется использование PKCS12    
`server.ssl.key-store=` - путь (относительный или абсолютный) до хранилища ключей (keystore)  
`server.ssl.key-store-password=` - пароль для keystore  
`server.ssl.key-password=` - пароль для ключей в keystore  
`server.ssl.protocol=TLS` - протокол работы  

В данном решении используется standalone сервер с встроенным https. Для корректной работы необходимо создать хранилище keystore в соответсвии с указанным в `server.ssl.key-store-type` форматом.  
Сгенерировать самоподписанный keystore с самоподписанным сертификатом можно следующей командой:  
`keytool -genkeypair -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048  -keystore keystore.p12 -validity 3650`  
Утилита keytool распространяется вместе со сборкой jdk, скачивать и устанавливать ее отдельно не нужно.

Так же для работы с самоподписанным сертификатом необходимо загрузить в Телеграм публичный сертификат в формате .pem  
Для того, чтобы выгрузить данный сертификат из только что созданного хранилища keystore можно воспользоваться openssl
`openssl pkcs12 -clcerts -nokeys -out pub.pem -in keystore.p12 -passin pass:"password"`

После успешной генерации необходимо корректно заполнить параметры в файле `external.properties` и при запуске приложение самостоятельно загрузит и разернет работающий https сервер и передаст необходимую информацию в Телеграм для дальнейшей работы. 

<h2>Параметры Telegram</h2>
Уникальные настройки для Телеграм находятся в следующих параметрах:  

`telegram.token=` - токен бота, присваивается присоздании
`telegram.username=` - имя бота  
`telegram.path=` - адрес сервера, например `https://example.com:8443/`

<h2>Проверка статуса приложения</h2>
Приложение фомрирует файл `status`, где содержится информация о текущем состоянии. Обновление информации происходит каждые 60 секунд.  
Формат информации следующий:  
`Время | статус | количество неотправленных сообщений | количество подключенных аккаунтов | всего активных аккаунтов`  
Файл автоматически генерируется в каталоге, откуда было запущено приложение  
