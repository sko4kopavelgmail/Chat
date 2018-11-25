# Chat
A simple chat with MySQL database.
You can create your DB with this script:

-- -----------------------------------------------------
-- Schema chat
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `chat` DEFAULT CHARACTER SET utf8 ;
USE `chat` ;

-- -----------------------------------------------------
-- Table `chat`.`User`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat`.`User` (
  `idUser` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idUser`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `chat`.`Message`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat`.`Message` (
  `idMessage` INT NOT NULL AUTO_INCREMENT,
  `Message` VARCHAR(1024) NOT NULL,
  PRIMARY KEY (`idMessage`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `chat`.`node`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat`.`node` (
  `idnode` INT NOT NULL AUTO_INCREMENT,
  `User_idUser` INT NOT NULL,
  `Message_idMessage` INT NOT NULL,
  `time` TIME NOT NULL,
  PRIMARY KEY (`idnode`),
  INDEX `fk_node_User_idx` (`User_idUser` ASC) VISIBLE,
  INDEX `fk_node_Message1_idx` (`Message_idMessage` ASC) VISIBLE,
  CONSTRAINT `fk_node_User`
    FOREIGN KEY (`User_idUser`)
    REFERENCES `chat`.`User` (`idUser`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_node_Message1`
    FOREIGN KEY (`Message_idMessage`)
    REFERENCES `chat`.`Message` (`idMessage`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;
