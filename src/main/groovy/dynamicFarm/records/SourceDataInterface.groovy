package dynamicFarm.records

interface SourceDataInterface <T> {

  /**
   *A method that gets the next data object from the source data object
   *
   * @return an object that can be incorporated into an instance of an object of type EmitInterface
   *         or null when there are no more objects.  This method will be called from the run method
   *         of an Emit process automatically.
   */
  T getSourceData()
}