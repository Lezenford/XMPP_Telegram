DROP TABLE IF EXISTS xmpp_accounts;

CREATE TABLE xmpp_accounts
(
  `id` INT NOT NULL AUTO_INCREMENT,
  `login` VARCHAR(250) NOT NULL,
  `password` VARCHAR(250) NOT NULL,
  `server` VARCHAR(250) NOT NULL,
  PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX xmpp_accounts_login_index ON xmpp_accounts (login,server);
