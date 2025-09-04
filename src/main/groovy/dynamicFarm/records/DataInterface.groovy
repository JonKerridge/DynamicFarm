package dynamicFarm.records

interface DataInterface<T> extends Serializable{

  /**
   * create() is used to generate a new instance of data objects in the Emit process,
   * once all the objects have been created it should return null
   *
   * The create method will use another constructor for the object that creates the data objects passed to
   * the rest of the process network.  The properties of the constructor will be created from
   * data held within the base object instance initialised when the object was initially constructed.
   *
   * If the application uses a source data file then
   *
   * @param sourceData an object containing source data for the emitted objects, obtained
   * from an object implementing the SourceDataInterface or null otherwise
   * @return A new instance of type T or null once all the data objects have been created
   */
  T create(Object sourceData)

}