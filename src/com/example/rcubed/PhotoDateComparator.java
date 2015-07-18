package com.example.rcubed;

import java.util.Comparator;

/**
 * Custom comparator for sorting Photos by their timestamp.
 */
public class PhotoDateComparator implements Comparator<Photo>
{
  /**
   * see java.util.compare(T arg0, T arg1)
   */
  @Override
  public int compare(Photo photo1, Photo photo2)
  {
    return photo1.getTimeStamp().compareTo(photo2.getTimeStamp());
  }
}
