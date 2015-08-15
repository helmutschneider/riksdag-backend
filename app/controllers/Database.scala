package controllers


import remote.Person
import sorm._

object  Db extends Instance(
  entities = Set() +Entity[Person](),
  url = "jdbc:mysql://theownagecool.se/riksdag_2",
  user = "romson",
  password = "vAd3XsVfc3Am37WW577N",
  initMode = InitMode.Create,
  poolSize = 1
)