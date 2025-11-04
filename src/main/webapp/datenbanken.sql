CREATE TABLE admin(id INT AUTO_INCREMENT PRIMARY KEY,email varchar(100) NOT NULL,password varchar(100)NOT NULL);
INSERT INTO admin(Email,password) values ("admin@gmail.com","admin123"); //Was ihr wollt



CREATE TABLE user(id INT AUTO_INCREMENT PRIMARY KEY,firstname varchar(100) NOT NULL, lastname varchar(100) NOT NULL, address varchar(100), email varchar(100) NOT NULL ,password varchar(100), salt varchar(128));

Create Table impfung(id int Auto_Increment Primary Key, Name varchar(255));


CREATE TABLE impfzentren (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(255) NOT NULL,
                             city VARCHAR(255) NOT NULL,
                             street VARCHAR(255) NOT NULL,
                             postalcode VARCHAR(20) NOT NULL
);

Create Table impfzentren_impfung(Center_id integer, Vaccine_id integer, Primary key
    (Center_id,Vaccine_id), Foreign key(Center_id) references Impfzentren
    (id) , Foreign key(Vaccine_id) references Impfung (id));


CREATE TABLE zeitslot (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          start_time TIMESTAMP ,
                          end_time Timestamp,
                          capacity INT ,
                          Center_id INT ,
                          FOREIGN KEY (Center_id) REFERENCES impfzentren(id)
);


CREATE TABLE buchung (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             user_id INT NOT NULL,
                             impfzentrum_id INT NOT NULL,
                             impfstoff_id INT NOT NULL,
                             timeslot_id INT NOT NULL,
                             vorname VARCHAR(255) NOT NULL,
                             nachname VARCHAR(255) NOT NULL,
                             addresse VARCHAR(255) NOT NULL,
                             datum DATETIME NOT NULL,
                             FOREIGN KEY (user_id) REFERENCES user(id),
                             FOREIGN KEY (impfzentrum_id) REFERENCES impfzentren(id),
                             FOREIGN KEY (impfstoff_id) REFERENCES impfung(id),
                             FOREIGN KEY (timeslot_id) REFERENCES zeitslot(id)
);