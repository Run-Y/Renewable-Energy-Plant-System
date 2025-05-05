package reps.service

object AlertSystem {
  def reportSystemState(code: Int): Unit = {
    code match {
      case 1 => println("System is normal.")
      case 2 => println("System is experiencing minor issues.")
      case 3 => println("Critical issue in the system!")
      case _ => println("Unknown system status.")
    }
  }
}

