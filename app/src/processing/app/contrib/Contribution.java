/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2013-16 The Processing Foundation
  Copyright (c) 2011-12 Ben Fry and Casey Reas

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.
  59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package processing.app.contrib;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import processing.app.Base;
import processing.core.PApplet;
import processing.data.StringDict;
import processing.data.StringList;
import processing.app.Language;
import processing.app.Util;


abstract public class Contribution {
  static final String IMPORTS_PROPERTY = "imports";
  static final String CATEGORIES_PROPERTY = "categories";
  static final String MODES_PROPERTY = "modes";
  static final String AUTHORS_PROPERTY = "authors";

  static final String UNKNOWN_CATEGORY = "Unknown";
  static final Set<String> validCategories =
    new HashSet<>(Arrays.asList(
      "3D", "Animation", "Data", "Geometry", "GUI", "Hardware",
      "I/O", "Math", "Renderer", "Simulation", "Sound",
      "Typography", "Utilities", "Video & Vision", "Other"
    ));

  static final String FOUNDATION_AUTHOR = "The Processing Foundation";

  protected StringList categories;  // "Sound", "Typography"
  protected String name;            // "pdf" or "PDF Export"
  protected String authors;         // [Ben Fry](http://benfry.com)
  protected String url;             // https://processing.org
  protected String sentence;        // Write graphics to PDF files.
  protected String paragraph;       // <paragraph length description for site>
  protected int version;            // 102
  protected String prettyVersion;   // "1.0.2"
  protected long lastUpdated;       // 1402805757
  protected int minRevision;        // 0
  protected int maxRevision;        // 227
  protected StringList imports;     // pdf.export,pdf.convert.common (list of packages, not imports)


  // "Sound", "Utilities"... see valid list in ContributionListing
  protected StringList getCategories() {
    return categories;
  }


  protected String getCategoryStr() {
    StringBuilder sb = new StringBuilder();
    for (String category : categories) {
      sb.append(category);
      sb.append(',');
    }
    sb.deleteCharAt(sb.length()-1);  // delete last comma
    return sb.toString();
  }


  protected boolean hasCategory(String category) {
    if (category != null) {
      for (String c : categories) {
        if (category.equalsIgnoreCase(c)) {
          return true;
        }
      }
    }
    return false;
  }


  // pdf.export.*,pdf.convert.common.*
  protected StringList getImports() {
    return imports;
  }


  /*
  protected boolean hasImport(String importName) {
    if (imports != null && importName != null) {
      for (String c : imports) {
        if (importName.equals(c)) {
          return true;
        }
      }
    }
    return false;
  }
  */


  // "pdf" or "PDF Export"
  public String getName() {
    return name;
  }


  // "[Ben Fry](http://benfry.com/)"
  public String getAuthorList() {
    return authors;
  }


  // "http://processing.org"
  public String getUrl() {
    return url;
  }


  // "Write graphics to PDF files."
  public String getSentence() {
    return sentence;
  }


  // <paragraph length description for site>
  public String getParagraph() {
    return paragraph;
  }


  // 102
  public int getVersion() {
    return version;
  }


  public void setPrettyVersion(String pretty) {
    if (pretty != null) {
      // some entries were written as "null", causing that to show in the ui
      if (pretty.equals("null") || pretty.length() == 0) {
        pretty = null;
      }
    }
    prettyVersion = pretty;
  }


  // "1.0.2" or null if not present
  public String getPrettyVersion() {
    return prettyVersion;
  }


  // returns prettyVersion, or "" if null
  public String getBenignVersion() {
    return (prettyVersion != null) ? prettyVersion : "";
  }


  // 1402805757
  public long getLastUpdated() {
    return lastUpdated;
  }


  // 0
  public int getMinRevision() {
    return minRevision;
  }


  // 227
  public int getMaxRevision() {
    return maxRevision;
  }


  public boolean isCompatible() {
    final int revisionNum = Base.getRevision();
    return ((maxRevision == 0 || revisionNum <= maxRevision) && revisionNum >= minRevision);
  }


  abstract public ContributionType getType();


  public String getTypeName() {
    return getType().toString();
  }


  abstract public boolean isInstalled();


//  /**
//   * Returns true if the type of contribution requires the PDE to restart
//   * when being added or removed.
//   */
//  public boolean requiresRestart() {
//    return getType() == ContributionType.TOOL || getType() == ContributionType.MODE;
//  }


  boolean isRestartFlagged() {
    return false;
  }


  /** Overridden by LocalContribution. */
  boolean isDeletionFlagged() {
    return false;
  }


  boolean isUpdateFlagged() {
    return false;
  }


  /**
   * Returns true if the contrib is from the Processing Foundation.
   */
  public boolean isFoundation() {
    return FOUNDATION_AUTHOR.equals(authors);
  }


  /*
  public StringDict loadProperties(File contribFolder) {
    return loadProperties(contribFolder, getType());
  }
  */


  /*
  static public StringDict loadProperties(File contribFolder,
                                          ContributionType type) {
    File propertiesFile = new File(contribFolder, type.getPropertiesName());
    if (propertiesFile.exists()) {
      return Util.readSettings(propertiesFile, false);
    }
    return null;
  }
  */


  /**
   * @return the list of categories that this contribution is part of
   *         (e.g. "Typography / Geometry"). "Unknown" if the category null.
   */
  static StringList parseCategories(StringDict properties) {
    StringList outgoing = new StringList();

    String categoryStr = properties.get(CATEGORIES_PROPERTY);
    if (categoryStr != null) {
      // Can't use splitTokens() because the names sometimes have spaces
      String[] listing = PApplet.trim(PApplet.split(categoryStr, ','));
      for (String category : listing) {
        if (validCategories.contains(category)) {
          category = translateCategory(category);
          outgoing.append(category);
        }
      }
    }
    if (outgoing.size() == 0) {
      outgoing.append(UNKNOWN_CATEGORY);
    }
    return outgoing;
  }


  /**
   * Returns the list of imports specified by this library author. Only
   * necessary for library authors that want to override the default behavior
   * of importing all packages in their library.
   * @return null if no entries found
   */
  static StringList parseImports(StringDict properties) {
    StringList outgoing = new StringList();

    String importStr = properties.get(IMPORTS_PROPERTY);
    if (importStr != null) {
      String[] importList = PApplet.trim(PApplet.split(importStr, ','));
      for (String importName : importList) {
        if (!importName.isEmpty()) {
          outgoing.append(importName);
        }
      }
    }
    return (outgoing.size() > 0) ? outgoing : null;
  }


  /**
   * Helper function that creates a StringList of the compatible Modes
   * for this Contribution.
   */
  static StringList parseModeList(StringDict properties) {
    String unparsedModes = properties.get(MODES_PROPERTY);

    // Workaround for 3.0 alpha/beta bug for 3.0b2
    if ("null".equals(unparsedModes)) {
      properties.remove(MODES_PROPERTY);
      unparsedModes = null;
    }

    StringList outgoing = new StringList();
    if (unparsedModes != null) {
      outgoing.append(PApplet.trim(PApplet.split(unparsedModes, ',')));
    }
    return outgoing;
  }


  static private String translateCategory(String cat) {
    // Converts Other to other, I/O to i_o, Video & Vision to video_vision
    String cleaned = cat.replaceAll("[\\W]+", "_").toLowerCase();
    return Language.text("contrib.category." + cleaned);
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o instanceof Contribution that) {
      return name.equalsIgnoreCase(that.name);
    }
    return false;
  }


  // TODO remove this because it hides actual differences in objects
  @Override
  public int hashCode() {
    return name.toLowerCase().hashCode();
  }


  @Override
  public String toString() {
    return getName() + " @" + Integer.toHexString(super.hashCode());
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public interface Filter {
    boolean matches(Contribution contrib);
  }
}
