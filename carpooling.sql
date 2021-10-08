USE sql10441832;

DROP TABLE IF EXISTS Calificaciones;
DROP TABLE IF EXISTS PasajerosPorViaje;
DROP TABLE IF EXISTS Solicitudes;
DROP TABLE IF EXISTS Viajes;
DROP TABLE IF EXISTS Notificaciones;
DROP TABLE IF EXISTS Usuarios;
DROP TABLE IF EXISTS Roles;
DROP TABLE IF EXISTS Ciudades;
DROP TABLE IF EXISTS Provincias;

CREATE TABLE Roles (
  Id varchar(3) NOT NULL,
  Nombre varchar(10) NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (Id)
  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE Provincias (
  Id int NOT NULL,
  Nombre varchar(30) NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (Id)
  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE Ciudades (
  Id int NOT NULL,
  ProvinciaId int NOT NULL,
  Nombre varchar(30) NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (Id),
  
  FOREIGN KEY (ProvinciaId)
	REFERENCES Provincias(Id)
  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE Usuarios (
  Email varchar(30) NOT NULL,
  Rol varchar(3) NOT NULL,
  Pass varchar(15) NOT NULL,
  Nombre varchar(20) NOT NULL,
  Apellido varchar(20) NOT NULL,
  Nacimiento datetime NOT NULL,
  Telefono varchar(20) NOT NULL,
  Dni varchar(8) NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (Email,Rol),
  
  FOREIGN KEY (Rol)
	REFERENCES Roles(Id)
    
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE Notificaciones (
  Id int NOT NULL,
  UsuarioEmail varchar(30) NOT NULL,
  UsuarioRol varchar(3) NOT NULL,
  Mensaje varchar(100) NOT NULL,
  EstadoNotificacion varchar(3) NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (Id),
  
  FOREIGN KEY (UsuarioEmail,UsuarioRol)
	REFERENCES Usuarios(Email,Rol)
  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE Viajes (
  Id int NOT NULL auto_increment,
  ConductorEmail varchar(30) NOT NULL,
  ProvinciaOrigenId int NOT NULL,
  CiudadOrigenId int NOT NULL,
  ProvinciaDestinoId int NOT NULL,
  CiudadDestinoId int NOT NULL,
  FechaHoraInicio datetime NOT NULL,
  FechaHoraFinalizacion datetime NOT NULL,
  CantidadPasajeros int NOT NULL,
  EstadoViaje varchar(3) NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (Id),
  
  FOREIGN KEY (ConductorEmail)
	REFERENCES Usuarios(Email),
    
  FOREIGN KEY (ProvinciaOrigenId,CiudadOrigenId)
	REFERENCES Ciudades(ProvinciaId, Id),
    
  FOREIGN KEY (ProvinciaDestinoId,CiudadDestinoId)
	REFERENCES Ciudades(ProvinciaId, Id)
    
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE Solicitudes (
  Id int NOT NULL auto_increment,
  PasajeroEmail varchar(30) NOT NULL,
  ProvinciaOrigenId int NOT NULL,
  CiudadOrigenId int NOT NULL,
  ProvinciaDestinoId int NOT NULL,
  CiudadDestinoId int NOT NULL,
  FechaHoraInicio datetime NOT NULL,
  CantidadAcompaniantes int NOT NULL,
  EstadoSolicitud varchar(3) NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (Id),
  
  FOREIGN KEY (PasajeroEmail)
	REFERENCES Usuarios(Email),
    
  FOREIGN KEY (ProvinciaOrigenId,CiudadOrigenId)
	REFERENCES Ciudades(ProvinciaId, Id),
    
  FOREIGN KEY (ProvinciaDestinoId,CiudadDestinoId)
	REFERENCES Ciudades(ProvinciaId, Id)
    
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE PasajerosPorViaje (
  ViajeId int NOT NULL,
  UsuarioEmail varchar(30) NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (ViajeId, UsuarioEmail),
  
  FOREIGN KEY (UsuarioEmail)
	REFERENCES Usuarios(Email),
    
  FOREIGN KEY (ViajeId)
	REFERENCES Viajes(Id)
  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE Calificaciones (
  Id int NOT NULL,
  UsuarioEmail varchar(30) NOT NULL,
  UsuarioRol varchar(3) NOT NULL,
  CalificadorEmail varchar(30) NOT NULL,
  CalificadorRol varchar(3) NOT NULL,
  ViajeId int NOT NULL,
  Calificacion smallint NOT NULL,
  EstadoRegistro boolean DEFAULT true,
  
  PRIMARY KEY (Id),
  
  FOREIGN KEY (UsuarioEmail, UsuarioRol)
	REFERENCES Usuarios(Email, Rol),

  FOREIGN KEY (CalificadorEmail, CalificadorRol)
	REFERENCES Usuarios(Email, Rol),

  FOREIGN KEY (ViajeId)
	REFERENCES Viajes(Id)
  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `Roles` (Id,Nombre,EstadoRegistro) VALUES("CON",'Conductor',1);
INSERT INTO `Roles` (Id,Nombre,EstadoRegistro) VALUES("PAS",'Pasajero',1);

INSERT INTO `Provincias` (Id,Nombre,EstadoRegistro) VALUES(100,'CABA',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1000,100,'Belgrano',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1001,100,'Retiro',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1002,100,'Palermo',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1003,100,'Saavedra',1);

INSERT INTO `Provincias` (Id,Nombre,EstadoRegistro) VALUES(101,'Buenos Aires',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1004,101,'Don Torcuato',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1005,101,'Mar del Plata',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1006,101,'San Pedro',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1007,101,'Tandil',1);

INSERT INTO `Usuarios` (`Email`,`Rol`,`Pass`,`Nombre`,`Apellido`,`Nacimiento`,`Telefono`,`Dni`,`EstadoRegistro`)
VALUES ("tobi@mail.com","CON","40379479","Tobias","Olea",'1997-05-12 00:00','+54 9 11 6920 3645','40379479',true);

INSERT INTO `Usuarios` (`Email`,`Rol`,`Pass`,`Nombre`,`Apellido`,`Nacimiento`,`Telefono`,`Dni`,`EstadoRegistro`)
VALUES ("joni@mail.com","PAS","40379479","Jonathan","Costa",'1996-05-12 00:00','+54 9 11 6920 3645','40379479',true);

INSERT INTO `Provincias` (Id,Nombre,EstadoRegistro) VALUES(102,'Entre Rios',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1008,102,'Concordia',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1009,102,'Parana',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1010,102,'Diamante',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1011,102,'Gualeguaychu',1);

INSERT INTO `Provincias` (Id,Nombre,EstadoRegistro) VALUES(103,'Cordoba',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1012,103,'Concordia',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1013,103,'Parana',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1014,103,'Diamante',1);
INSERT INTO `Ciudades` (Id,ProvinciaId,Nombre,EstadoRegistro) VALUES(1015,103,'Gualeguaychu',1);