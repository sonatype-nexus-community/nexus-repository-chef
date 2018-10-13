/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.chef.internal.util;

import java.io.InputStream;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.chef.internal.AssetKind;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class CookBookApiAbsoluteUrlRemoverTest
    extends TestSupport
{
  private CookBookApiAbsoluteUrlRemover underTest;

  @Before
  public void setUp() throws Exception {
    ChefDataAccess chefDataAccess = new ChefDataAccess();
    underTest = new CookBookApiAbsoluteUrlRemover(chefDataAccess);
  }

  @Test
  public void maybeRewriteCookbookApiResponseAbsoluteUrlsCookbookDetailsTest() throws Exception
  {
    InputStream is = getClass().getResourceAsStream("cookbookdetails.json");
    Content content = new Content(new StreamPayload(new InputStreamSupplierTest(is), is.available(),
        ContentTypes.APPLICATION_JSON));
    Content result = underTest.maybeRewriteCookbookApiResponseAbsoluteUrls(content, AssetKind.COOKBOOK_DETAILS);

    JSONAssert.assertEquals(
        IOUtils.toString(getClass().getResourceAsStream("cookbookdetails_result.json")),
        IOUtils.toString(result.openInputStream()),
        false);
  }

  @Test
  public void maybeRewriteCookbookApiResponseAbsoluteUrlsCookbookDetailVersionTest() throws Exception
  {
    InputStream is = getClass().getResourceAsStream("cookbookdetailversion.json");
    Content content = new Content(new StreamPayload(new InputStreamSupplierTest(is), is.available(),
        ContentTypes.APPLICATION_JSON));
    Content result = underTest.maybeRewriteCookbookApiResponseAbsoluteUrls(content, AssetKind.COOKBOOK_DETAIL_VERSION);

    JSONAssert.assertEquals(
        IOUtils.toString(getClass().getResourceAsStream("cookbookdetailversion_result.json")),
        IOUtils.toString(result.openInputStream()),
        false);
  }

  @Test
  public void maybeRewriteCookbookApiResponseAbsoluteUrlsCookbookListTest() throws Exception
  {
    InputStream is = getClass().getResourceAsStream("cookbooklist.json");
    Content content = new Content(new StreamPayload(new InputStreamSupplierTest(is), is.available(),
        ContentTypes.APPLICATION_JSON));
    Content result = underTest.maybeRewriteCookbookApiResponseAbsoluteUrls(content, AssetKind.COOKBOOKS_LIST);

    JSONAssert.assertEquals(
        IOUtils.toString(getClass().getResourceAsStream("cookbooklist_result.json")),
        IOUtils.toString(result.openInputStream()),
        false);
  }

  @Test
  public void maybeRewriteCookbookApiResponseAbsoluteUrlsCookbookSearchTest() throws Exception
  {
    InputStream is = getClass().getResourceAsStream("cookbooksearch.json");
    Content content = new Content(new StreamPayload(new InputStreamSupplierTest(is), is.available(),
        ContentTypes.APPLICATION_JSON));
    Content result = underTest.maybeRewriteCookbookApiResponseAbsoluteUrls(content, AssetKind.COOKBOOKS_SEARCH);

    JSONAssert.assertEquals(
        IOUtils.toString(getClass().getResourceAsStream("cookbooksearch_result.json")),
        IOUtils.toString(result.openInputStream()),
        false);
  }

  private class InputStreamSupplierTest
      implements InputStreamSupplier
  {
    private InputStream is;

    public InputStreamSupplierTest(final InputStream is) {
      this.is = is;
    }

    @Override
    public InputStream get() {
      return this.is;
    }

  }
}
