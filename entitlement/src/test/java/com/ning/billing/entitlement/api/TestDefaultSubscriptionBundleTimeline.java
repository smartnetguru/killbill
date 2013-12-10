/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.entitlement.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.CatalogApiException;
import com.ning.billing.catalog.api.Plan;
import com.ning.billing.catalog.api.PlanPhase;
import com.ning.billing.catalog.api.PriceList;
import com.ning.billing.catalog.api.Product;
import com.ning.billing.entitlement.DefaultEntitlementService;
import com.ning.billing.entitlement.EntitlementTestSuiteNoDB;
import com.ning.billing.junction.DefaultBlockingState;
import com.ning.billing.subscription.api.SubscriptionBase;
import com.ning.billing.subscription.api.user.SubscriptionBaseTransition;
import com.ning.billing.subscription.api.user.SubscriptionBaseTransitionData;
import com.ning.billing.subscription.events.SubscriptionBaseEvent.EventType;
import com.ning.billing.subscription.events.user.ApiEventType;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class TestDefaultSubscriptionBundleTimeline extends EntitlementTestSuiteNoDB {

    private UUID bundleId;

    @BeforeClass(groups = "fast")
    protected void beforeClass() throws Exception {
        super.beforeClass();
        bundleId = UUID.randomUUID();
    }

    public class TestSubscriptionBundleTimeline extends DefaultSubscriptionBundleTimeline {

        public TestSubscriptionBundleTimeline(final DateTimeZone accountTimeZone, final UUID accountId, final UUID bundleId, final String externalKey, final List<Entitlement> entitlements, final List<BlockingState> allBlockingStates) {
            super(accountTimeZone, accountId, bundleId, externalKey, entitlements, allBlockingStates);
        }

        public SubscriptionEvent createEvent(final UUID subscriptionId, final SubscriptionEventType type, final DateTime effectiveDate) {
            return new DefaultSubscriptionEvent(UUID.randomUUID(),
                                                subscriptionId,
                                                effectiveDate,
                                                null,
                                                type,
                                                true,
                                                true,
                                                "foo",
                                                "bar",
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null);

        }
    }

    @Test(groups = "fast")
    public void testReOrderSubscriptionEventsOnInvalidOrder1() {
        final TestSubscriptionBundleTimeline timeline = new TestSubscriptionBundleTimeline(null, null, null, null, new ArrayList<Entitlement>(), new ArrayList<BlockingState>());

        final List<SubscriptionEvent> events = new ArrayList<SubscriptionEvent>();
        final UUID subscriptionId = UUID.randomUUID();
        final DateTime effectiveDate = clock.getUTCNow();
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));

        timeline.reOrderSubscriptionEventsOnSameDateByType(events);

        Assert.assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        Assert.assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        Assert.assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        Assert.assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
    }

    @Test(groups = "fast")
    public void testReOrderSubscriptionEventsOnInvalidOrder2() {
        final TestSubscriptionBundleTimeline timeline = new TestSubscriptionBundleTimeline(null, null, null, null, new ArrayList<Entitlement>(), new ArrayList<BlockingState>());

        final List<SubscriptionEvent> events = new ArrayList<SubscriptionEvent>();
        final UUID subscriptionId = UUID.randomUUID();
        final DateTime effectiveDate = clock.getUTCNow();
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_ENTITLEMENT, effectiveDate));

        timeline.reOrderSubscriptionEventsOnSameDateByType(events);

        Assert.assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        Assert.assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        Assert.assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        Assert.assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
    }

    @Test(groups = "fast")
    public void testReOrderSubscriptionEventsOnInvalidOrder3() {
        final TestSubscriptionBundleTimeline timeline = new TestSubscriptionBundleTimeline(null, null, null, null, new ArrayList<Entitlement>(), new ArrayList<BlockingState>());

        final List<SubscriptionEvent> events = new ArrayList<SubscriptionEvent>();
        final UUID subscriptionId = UUID.randomUUID();
        final DateTime effectiveDate = clock.getUTCNow();
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_ENTITLEMENT, effectiveDate));

        timeline.reOrderSubscriptionEventsOnSameDateByType(events);

        Assert.assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        Assert.assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        Assert.assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        Assert.assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
    }

    @Test(groups = "fast")
    public void testReOrderSubscriptionEventsOnInvalidOrderAndDifferentSubscriptionsSameDates1() {
        final TestSubscriptionBundleTimeline timeline = new TestSubscriptionBundleTimeline(null, null, null, null, new ArrayList<Entitlement>(), new ArrayList<BlockingState>());

        final List<SubscriptionEvent> events = new ArrayList<SubscriptionEvent>();
        final UUID subscriptionId = UUID.fromString("60b64e0c-cefd-48c3-8de9-c731a9558165");

        final UUID otherSubscriptionId = UUID.fromString("35b3b340-31b2-46ea-b062-e9fc9fab3bc9");
        final DateTime effectiveDate = clock.getUTCNow();

        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));
        events.add(timeline.createEvent(otherSubscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_ENTITLEMENT, effectiveDate));

        timeline.reOrderSubscriptionEventsOnSameDateByType(events);

        Assert.assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
        Assert.assertEquals(events.get(0).getEntitlementId(), otherSubscriptionId);
        Assert.assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        Assert.assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        Assert.assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        Assert.assertEquals(events.get(4).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
        Assert.assertEquals(events.get(4).getEntitlementId(), subscriptionId);
    }

    @Test(groups = "fast")
    public void testReOrderSubscriptionEventsOnInvalidOrderAndDifferentSubscriptionsSameDates2() {
        final TestSubscriptionBundleTimeline timeline = new TestSubscriptionBundleTimeline(null, null, null, null, new ArrayList<Entitlement>(), new ArrayList<BlockingState>());

        final List<SubscriptionEvent> events = new ArrayList<SubscriptionEvent>();
        final UUID subscriptionId = UUID.fromString("35b3b340-31b2-46ea-b062-e9fc9fab3bc9");
        final UUID otherSubscriptionId = UUID.fromString("60b64e0c-cefd-48c3-8de9-c731a9558165");

        final DateTime effectiveDate = clock.getUTCNow();

        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));
        events.add(timeline.createEvent(otherSubscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_ENTITLEMENT, effectiveDate));

        timeline.reOrderSubscriptionEventsOnSameDateByType(events);

        Assert.assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        Assert.assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        Assert.assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        Assert.assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
        Assert.assertEquals(events.get(3).getEntitlementId(), subscriptionId);
        Assert.assertEquals(events.get(4).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
        Assert.assertEquals(events.get(4).getEntitlementId(), otherSubscriptionId);

    }

    @Test(groups = "fast")
    public void testReOrderSubscriptionEventsOnInvalidOrderAndDifferentSubscriptionsDates() {
        final TestSubscriptionBundleTimeline timeline = new TestSubscriptionBundleTimeline(null, null, null, null, new ArrayList<Entitlement>(), new ArrayList<BlockingState>());

        final List<SubscriptionEvent> events = new ArrayList<SubscriptionEvent>();
        final UUID subscriptionId = UUID.randomUUID();

        final UUID otherSubscriptionId = UUID.randomUUID();
        final DateTime effectiveDate = clock.getUTCNow();
        final DateTime otherEffectiveDate = clock.getUTCNow().plusDays(1);

        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_ENTITLEMENT, effectiveDate));

        events.add(timeline.createEvent(otherSubscriptionId, SubscriptionEventType.START_BILLING, otherEffectiveDate));
        events.add(timeline.createEvent(otherSubscriptionId, SubscriptionEventType.START_ENTITLEMENT, otherEffectiveDate));
        events.add(timeline.createEvent(otherSubscriptionId, SubscriptionEventType.STOP_ENTITLEMENT, otherEffectiveDate));
        events.add(timeline.createEvent(otherSubscriptionId, SubscriptionEventType.STOP_BILLING, otherEffectiveDate));
        events.add(timeline.createEvent(otherSubscriptionId, SubscriptionEventType.PAUSE_ENTITLEMENT, otherEffectiveDate));
        events.add(timeline.createEvent(otherSubscriptionId, SubscriptionEventType.PAUSE_BILLING, otherEffectiveDate));

        timeline.reOrderSubscriptionEventsOnSameDateByType(events);

        Assert.assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        Assert.assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        Assert.assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        Assert.assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);

        Assert.assertEquals(events.get(4).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        Assert.assertEquals(events.get(5).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        Assert.assertEquals(events.get(6).getSubscriptionEventType(), SubscriptionEventType.PAUSE_ENTITLEMENT);
        Assert.assertEquals(events.get(7).getSubscriptionEventType(), SubscriptionEventType.PAUSE_BILLING);
        Assert.assertEquals(events.get(8).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        Assert.assertEquals(events.get(9).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
    }

    @Test(groups = "fast")
    public void testReOrderSubscriptionEventsOnCorrectOrder() {
        final TestSubscriptionBundleTimeline timeline = new TestSubscriptionBundleTimeline(null, null, null, null, new ArrayList<Entitlement>(), new ArrayList<BlockingState>());

        final List<SubscriptionEvent> events = new ArrayList<SubscriptionEvent>();
        final UUID subscriptionId = UUID.randomUUID();
        final DateTime effectiveDate = clock.getUTCNow();
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.START_BILLING, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_ENTITLEMENT, effectiveDate));
        events.add(timeline.createEvent(subscriptionId, SubscriptionEventType.STOP_BILLING, effectiveDate));

        timeline.reOrderSubscriptionEventsOnSameDateByType(events);

        Assert.assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        Assert.assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        Assert.assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        Assert.assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
    }

    @Test(groups = "fast")
    public void testOneEntitlementNoBlockingStates() throws CatalogApiException {
        clock.setDay(new LocalDate(2013, 1, 1));

        final DateTimeZone accountTimeZone = DateTimeZone.UTC;
        final UUID accountId = UUID.randomUUID();
        final UUID bundleId = UUID.randomUUID();
        final String externalKey = "foo";

        final UUID entitlementId = UUID.randomUUID();

        final List<SubscriptionBaseTransition> allTransitions = new ArrayList<SubscriptionBaseTransition>();

        final DateTime requestedDate = new DateTime();
        DateTime effectiveDate = new DateTime(2013, 1, 1, 15, 43, 25, 0, DateTimeZone.UTC);
        final SubscriptionBaseTransition tr1 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CREATE, requestedDate, effectiveDate, clock.getUTCNow(), null, "trial");
        allTransitions.add(tr1);

        effectiveDate = effectiveDate.plusDays(30);
        clock.addDays(30);
        final SubscriptionBaseTransition tr2 = createTransition(entitlementId, EventType.PHASE, null, requestedDate, effectiveDate, clock.getUTCNow(), "trial", "phase");
        allTransitions.add(tr2);

        effectiveDate = effectiveDate.plusDays(15);
        clock.addDays(15);
        final SubscriptionBaseTransition tr3 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CANCEL, requestedDate, effectiveDate, clock.getUTCNow(), "phase", null);
        allTransitions.add(tr3);

        final List<Entitlement> entitlements = new ArrayList<Entitlement>();
        final Entitlement entitlement = createEntitlement(entitlementId, allTransitions);
        entitlements.add(entitlement);

        final SubscriptionBundleTimeline timeline = new DefaultSubscriptionBundleTimeline(accountTimeZone, accountId, bundleId, externalKey, entitlements, Collections.<BlockingState>emptyList());

        assertEquals(timeline.getAccountId(), accountId);
        assertEquals(timeline.getBundleId(), bundleId);
        assertEquals(timeline.getExternalKey(), externalKey);

        final List<SubscriptionEvent> events = timeline.getSubscriptionEvents();
        assertEquals(events.size(), 4);

        assertEquals(events.get(0).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(1).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(2).getEffectiveDate().compareTo(new LocalDate(tr2.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(3).getEffectiveDate().compareTo(new LocalDate(tr3.getEffectiveTransitionTime(), accountTimeZone)), 0);

        assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.PHASE);
        assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);

        assertEquals(events.get(0).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(1).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);
        assertEquals(events.get(2).getServiceName(), DefaultSubscriptionBundleTimeline.ENT_BILLING_SERVICE_NAME);
        assertEquals(events.get(3).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);

        assertNull(events.get(0).getPrevPhase());
        assertEquals(events.get(0).getNextPhase().getName(), "trial");
        assertNull(events.get(1).getPrevPhase());
        assertEquals(events.get(1).getNextPhase().getName(), "trial");
        assertEquals(events.get(2).getPrevPhase().getName(), "trial");
        assertEquals(events.get(2).getNextPhase().getName(), "phase");
        assertEquals(events.get(3).getPrevPhase().getName(), "phase");
        assertNull(events.get(3).getNextPhase());
    }

    @Test(groups = "fast", description = "Test for https://github.com/killbill/killbill/issues/135")
    public void testOneEntitlementWithPauseResume() throws CatalogApiException {
        clock.setDay(new LocalDate(2013, 1, 1));

        final DateTimeZone accountTimeZone = DateTimeZone.UTC;
        final UUID accountId = UUID.randomUUID();
        final UUID bundleId = UUID.randomUUID();
        final String externalKey = "foo";

        final UUID entitlementId = UUID.randomUUID();

        final List<SubscriptionBaseTransition> allTransitions = new ArrayList<SubscriptionBaseTransition>();
        final List<BlockingState> blockingStates = new ArrayList<BlockingState>();

        final DateTime requestedDate = new DateTime();
        DateTime effectiveDate = new DateTime(2013, 1, 1, 15, 43, 25, 0, DateTimeZone.UTC);
        final SubscriptionBaseTransition tr1 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CREATE, requestedDate, effectiveDate, clock.getUTCNow(), null, "trial");
        allTransitions.add(tr1);

        effectiveDate = effectiveDate.plusDays(30);
        clock.addDays(30);
        final SubscriptionBaseTransition tr2 = createTransition(entitlementId, EventType.PHASE, null, requestedDate, effectiveDate, clock.getUTCNow(), "trial", "phase");
        allTransitions.add(tr2);

        effectiveDate = effectiveDate.plusDays(12);
        clock.addDays(12);
        final BlockingState bs1 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           "NothingUseful1", DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, true, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs1);

        effectiveDate = effectiveDate.plusDays(42);
        clock.addDays(42);
        final BlockingState bs2 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           "NothingUseful2", DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           false, false, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs2);

        effectiveDate = effectiveDate.plusDays(15);
        clock.addDays(15);
        final String service = "boo";
        final BlockingState bs3 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           "NothingUseful3", service,
                                                           false, false, true, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs3);

        effectiveDate = effectiveDate.plusDays(15);
        clock.addDays(15);
        final BlockingState bs4 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           "NothingUseful4", service,
                                                           false, false, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs4);

        final List<Entitlement> entitlements = new ArrayList<Entitlement>();
        final Entitlement entitlement = createEntitlement(entitlementId, allTransitions);
        entitlements.add(entitlement);

        final SubscriptionBundleTimeline timeline = new DefaultSubscriptionBundleTimeline(accountTimeZone, accountId, bundleId, externalKey, entitlements, blockingStates);

        assertEquals(timeline.getAccountId(), accountId);
        assertEquals(timeline.getBundleId(), bundleId);
        assertEquals(timeline.getExternalKey(), externalKey);

        final List<SubscriptionEvent> events = timeline.getSubscriptionEvents();
        assertEquals(events.size(), 9);

        assertEquals(events.get(0).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(1).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(2).getEffectiveDate().compareTo(new LocalDate(tr2.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(3).getEffectiveDate().compareTo(new LocalDate(bs1.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(4).getEffectiveDate().compareTo(new LocalDate(bs1.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(5).getEffectiveDate().compareTo(new LocalDate(bs2.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(6).getEffectiveDate().compareTo(new LocalDate(bs2.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(7).getEffectiveDate().compareTo(new LocalDate(bs3.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(8).getEffectiveDate().compareTo(new LocalDate(bs4.getEffectiveDate(), accountTimeZone)), 0);

        assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);

        assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.PHASE);

        assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.PAUSE_ENTITLEMENT);
        assertEquals(events.get(4).getSubscriptionEventType(), SubscriptionEventType.PAUSE_BILLING);
        assertEquals(events.get(5).getSubscriptionEventType(), SubscriptionEventType.RESUME_ENTITLEMENT);
        assertEquals(events.get(6).getSubscriptionEventType(), SubscriptionEventType.RESUME_BILLING);

        assertEquals(events.get(7).getSubscriptionEventType(), SubscriptionEventType.SERVICE_STATE_CHANGE);
        assertEquals(events.get(8).getSubscriptionEventType(), SubscriptionEventType.SERVICE_STATE_CHANGE);

        assertEquals(events.get(0).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(1).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);

        assertEquals(events.get(2).getServiceName(), DefaultSubscriptionBundleTimeline.ENT_BILLING_SERVICE_NAME);

        assertEquals(events.get(3).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(4).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);
        assertEquals(events.get(5).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(6).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);

        assertEquals(events.get(7).getServiceName(), service);
        assertEquals(events.get(8).getServiceName(), service);

        assertNull(events.get(0).getPrevPhase());
        assertEquals(events.get(0).getNextPhase().getName(), "trial");
        assertNull(events.get(1).getPrevPhase());
        assertEquals(events.get(1).getNextPhase().getName(), "trial");

        assertEquals(events.get(2).getPrevPhase().getName(), "trial");
        assertEquals(events.get(2).getNextPhase().getName(), "phase");

        assertEquals(events.get(3).getPrevPhase().getName(), "phase");
        assertEquals(events.get(3).getNextPhase().getName(), "phase");
        assertEquals(events.get(4).getPrevPhase().getName(), "phase");
        assertEquals(events.get(4).getNextPhase().getName(), "phase");
        assertEquals(events.get(5).getPrevPhase().getName(), "phase");
        assertEquals(events.get(5).getNextPhase().getName(), "phase");
        assertEquals(events.get(6).getPrevPhase().getName(), "phase");
        assertEquals(events.get(6).getNextPhase().getName(), "phase");
        assertEquals(events.get(7).getPrevPhase().getName(), "phase");
        assertEquals(events.get(7).getNextPhase().getName(), "phase");
        assertEquals(events.get(8).getPrevPhase().getName(), "phase");
        assertEquals(events.get(8).getNextPhase().getName(), "phase");
    }

    @Test(groups = "fast")
    public void testOneEntitlementWithInitialBlockingState() throws CatalogApiException {
        clock.setDay(new LocalDate(2013, 1, 1));

        final DateTimeZone accountTimeZone = DateTimeZone.UTC;
        final UUID accountId = UUID.randomUUID();
        final UUID bundleId = UUID.randomUUID();
        final String externalKey = "foo";

        final UUID entitlementId = UUID.randomUUID();

        final List<SubscriptionBaseTransition> allTransitions = new ArrayList<SubscriptionBaseTransition>();
        final List<BlockingState> blockingStates = new ArrayList<BlockingState>();
        final BlockingState bs1 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           DefaultEntitlementApi.ENT_STATE_BLOCKED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, clock.getUTCNow(), clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs1);

        clock.addDays(1);

        final DateTime requestedDate = new DateTime();
        DateTime effectiveDate = new DateTime(2013, 1, 1, 15, 43, 25, 0, DateTimeZone.UTC);
        final SubscriptionBaseTransition tr1 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CREATE, requestedDate, effectiveDate, clock.getUTCNow(), null, "trial");
        allTransitions.add(tr1);

        effectiveDate = effectiveDate.plusDays(30);
        clock.addDays(30);
        final SubscriptionBaseTransition tr2 = createTransition(entitlementId, EventType.PHASE, null, requestedDate, effectiveDate, clock.getUTCNow(), "trial", "phase");
        allTransitions.add(tr2);

        final String service = "boo";
        final BlockingState bs2 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           "NothingUseful", service,
                                                           false, false, false, clock.getUTCNow(), clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs2);

        effectiveDate = effectiveDate.plusDays(15);
        clock.addDays(15);
        final SubscriptionBaseTransition tr3 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CANCEL, requestedDate, effectiveDate, clock.getUTCNow(), "phase", null);
        allTransitions.add(tr3);

        final List<Entitlement> entitlements = new ArrayList<Entitlement>();
        final Entitlement entitlement = createEntitlement(entitlementId, allTransitions);
        entitlements.add(entitlement);

        final SubscriptionBundleTimeline timeline = new DefaultSubscriptionBundleTimeline(accountTimeZone, accountId, bundleId, externalKey, entitlements, blockingStates);

        assertEquals(timeline.getAccountId(), accountId);
        assertEquals(timeline.getBundleId(), bundleId);
        assertEquals(timeline.getExternalKey(), externalKey);

        final List<SubscriptionEvent> events = timeline.getSubscriptionEvents();
        assertEquals(events.size(), 5);

        assertEquals(events.get(0).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(1).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(2).getEffectiveDate().compareTo(new LocalDate(tr2.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(3).getEffectiveDate().compareTo(new LocalDate(bs2.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(4).getEffectiveDate().compareTo(new LocalDate(tr3.getEffectiveTransitionTime(), accountTimeZone)), 0);

        assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.PHASE);
        assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.SERVICE_STATE_CHANGE);
        assertEquals(events.get(4).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);

        assertEquals(events.get(0).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(1).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);
        assertEquals(events.get(2).getServiceName(), DefaultSubscriptionBundleTimeline.ENT_BILLING_SERVICE_NAME);
        assertEquals(events.get(3).getServiceName(), service);
        assertEquals(events.get(4).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);

        assertNull(events.get(0).getPrevPhase());
        assertEquals(events.get(0).getNextPhase().getName(), "trial");
        assertNull(events.get(1).getPrevPhase());
        assertEquals(events.get(1).getNextPhase().getName(), "trial");
        assertEquals(events.get(2).getPrevPhase().getName(), "trial");
        assertEquals(events.get(2).getNextPhase().getName(), "phase");
        assertEquals(events.get(3).getPrevPhase().getName(), "phase");
        assertEquals(events.get(3).getNextPhase().getName(), "phase");
        assertEquals(events.get(4).getPrevPhase().getName(), "phase");
        assertNull(events.get(4).getNextPhase());
    }

    @Test(groups = "fast")
    public void testOneEntitlementWithBlockingStatesSubscription() throws CatalogApiException {
        clock.setDay(new LocalDate(2013, 1, 1));

        final DateTimeZone accountTimeZone = DateTimeZone.UTC;
        final UUID accountId = UUID.randomUUID();
        final UUID bundleId = UUID.randomUUID();
        final String externalKey = "foo";

        final UUID entitlementId = UUID.randomUUID();

        final List<SubscriptionBaseTransition> allTransitions = new ArrayList<SubscriptionBaseTransition>();
        final List<BlockingState> blockingStates = new ArrayList<BlockingState>();

        final DateTime requestedDate = new DateTime();
        DateTime effectiveDate = new DateTime(2013, 1, 1, 15, 43, 25, 0, DateTimeZone.UTC);
        final SubscriptionBaseTransition tr1 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CREATE, requestedDate, effectiveDate, clock.getUTCNow(), null, "trial");
        allTransitions.add(tr1);

        effectiveDate = effectiveDate.plusDays(30);
        clock.addDays(30);
        final SubscriptionBaseTransition tr2 = createTransition(entitlementId, EventType.PHASE, null, requestedDate, effectiveDate, clock.getUTCNow(), "trial", "phase");
        allTransitions.add(tr2);

        effectiveDate = effectiveDate.plusDays(5);
        clock.addDays(5);
        final BlockingState bs1 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           DefaultEntitlementApi.ENT_STATE_BLOCKED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());
        blockingStates.add(bs1);

        effectiveDate = effectiveDate.plusDays(15);
        clock.addDays(15);
        final SubscriptionBaseTransition tr3 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CANCEL, requestedDate, effectiveDate, clock.getUTCNow(), "phase", null);
        allTransitions.add(tr3);
        final BlockingState bs2 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           DefaultEntitlementApi.ENT_STATE_CANCELLED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs2);

        final List<Entitlement> entitlements = new ArrayList<Entitlement>();
        final Entitlement entitlement = createEntitlement(entitlementId, allTransitions);
        entitlements.add(entitlement);

        final SubscriptionBundleTimeline timeline = new DefaultSubscriptionBundleTimeline(accountTimeZone, accountId, bundleId, externalKey, entitlements, blockingStates);

        final List<SubscriptionEvent> events = timeline.getSubscriptionEvents();
        assertEquals(events.size(), 6);

        assertEquals(events.get(0).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(1).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(2).getEffectiveDate().compareTo(new LocalDate(tr2.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(3).getEffectiveDate().compareTo(new LocalDate(bs1.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(4).getEffectiveDate().compareTo(new LocalDate(tr3.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(5).getEffectiveDate().compareTo(new LocalDate(bs2.getEffectiveDate(), accountTimeZone)), 0);

        assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.PHASE);
        assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.PAUSE_ENTITLEMENT);
        assertEquals(events.get(4).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        assertEquals(events.get(5).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);

        assertEquals(events.get(0).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(1).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);
        assertEquals(events.get(2).getServiceName(), DefaultSubscriptionBundleTimeline.ENT_BILLING_SERVICE_NAME);
        assertEquals(events.get(3).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(4).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(5).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);

        assertNull(events.get(0).getPrevPhase());
        assertEquals(events.get(0).getNextPhase().getName(), "trial");
        assertNull(events.get(1).getPrevPhase());
        assertEquals(events.get(1).getNextPhase().getName(), "trial");

        assertEquals(events.get(2).getPrevPhase().getName(), "trial");
        assertEquals(events.get(2).getNextPhase().getName(), "phase");

        assertEquals(events.get(3).getPrevPhase().getName(), "phase");
        assertEquals(events.get(3).getNextPhase().getName(), "phase");

        assertEquals(events.get(4).getPrevPhase().getName(), "phase");
        assertNull(events.get(4).getNextPhase());
        assertEquals(events.get(5).getPrevPhase().getName(), "phase");
        assertNull(events.get(5).getNextPhase());
    }

    @Test(groups = "fast")
    public void testWithMultipleEntitlements() throws CatalogApiException {
        clock.setDay(new LocalDate(2013, 1, 1));

        final DateTimeZone accountTimeZone = DateTimeZone.UTC;
        final UUID accountId = UUID.randomUUID();
        final UUID bundleId = UUID.randomUUID();
        final String externalKey = "foo";

        final UUID entitlementId1 = UUID.fromString("cf5a597a-cf15-45d3-8f02-95371be7f927");
        final UUID entitlementId2 = UUID.fromString("e37cc97a-7b98-4ab6-a29a-7259e45c3366");

        final List<SubscriptionBaseTransition> allTransitions1 = new ArrayList<SubscriptionBaseTransition>();
        final List<SubscriptionBaseTransition> allTransitions2 = new ArrayList<SubscriptionBaseTransition>();
        final List<BlockingState> blockingStates = new ArrayList<BlockingState>();

        final DateTime requestedDate = new DateTime();
        DateTime effectiveDate = new DateTime(2013, 1, 1, 15, 43, 25, 0, DateTimeZone.UTC);
        final SubscriptionBaseTransition ent1Tr1 = createTransition(entitlementId1, EventType.API_USER, ApiEventType.CREATE, requestedDate, effectiveDate, clock.getUTCNow(), null, "trial1");
        allTransitions1.add(ent1Tr1);

        effectiveDate = effectiveDate.plusDays(15);
        clock.addDays(15);
        final SubscriptionBaseTransition ent2Tr1 = createTransition(entitlementId2, EventType.API_USER, ApiEventType.TRANSFER, requestedDate, effectiveDate, clock.getUTCNow(), null, "phase2");
        allTransitions2.add(ent2Tr1);

        effectiveDate = effectiveDate.plusDays(15);
        clock.addDays(15);
        final SubscriptionBaseTransition ent1Tr2 = createTransition(entitlementId1, EventType.PHASE, null, requestedDate, effectiveDate, clock.getUTCNow(), "trial1", "phase1");
        allTransitions1.add(ent1Tr2);

        effectiveDate = effectiveDate.plusDays(5);
        clock.addDays(5);
        final BlockingState bs1 = new DefaultBlockingState(UUID.randomUUID(), bundleId, BlockingStateType.SUBSCRIPTION_BUNDLE,
                                                           DefaultEntitlementApi.ENT_STATE_BLOCKED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());
        blockingStates.add(bs1);

        effectiveDate = effectiveDate.plusDays(15);
        clock.addDays(15);
        final SubscriptionBaseTransition ent1Tr3 = createTransition(entitlementId1, EventType.API_USER, ApiEventType.CANCEL, requestedDate, effectiveDate, clock.getUTCNow(), "phase1", null);
        allTransitions1.add(ent1Tr3);
        final BlockingState bs2 = new DefaultBlockingState(UUID.randomUUID(), entitlementId1, BlockingStateType.SUBSCRIPTION,
                                                           DefaultEntitlementApi.ENT_STATE_CANCELLED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs2);

        final List<Entitlement> entitlements = new ArrayList<Entitlement>();
        final Entitlement entitlement1 = createEntitlement(entitlementId1, allTransitions1);
        entitlements.add(entitlement1);

        final Entitlement entitlement2 = createEntitlement(entitlementId2, allTransitions2);
        entitlements.add(entitlement2);

        final SubscriptionBundleTimeline timeline = new DefaultSubscriptionBundleTimeline(accountTimeZone, accountId, bundleId, externalKey, entitlements, blockingStates);

        final List<SubscriptionEvent> events = timeline.getSubscriptionEvents();
        assertEquals(events.size(), 9);

        assertEquals(events.get(0).getEffectiveDate().compareTo(new LocalDate(ent1Tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(1).getEffectiveDate().compareTo(new LocalDate(ent1Tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(2).getEffectiveDate().compareTo(new LocalDate(ent2Tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(3).getEffectiveDate().compareTo(new LocalDate(ent2Tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);

        assertEquals(events.get(4).getEffectiveDate().compareTo(new LocalDate(ent1Tr2.getEffectiveTransitionTime(), accountTimeZone)), 0);

        assertEquals(events.get(5).getEffectiveDate().compareTo(new LocalDate(bs1.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(6).getEffectiveDate().compareTo(new LocalDate(bs1.getEffectiveDate(), accountTimeZone)), 0);

        assertEquals(events.get(7).getEffectiveDate().compareTo(new LocalDate(ent1Tr3.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(8).getEffectiveDate().compareTo(new LocalDate(bs2.getEffectiveDate(), accountTimeZone)), 0);

        assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);

        assertEquals(events.get(4).getSubscriptionEventType(), SubscriptionEventType.PHASE);

        assertEquals(events.get(5).getSubscriptionEventType(), SubscriptionEventType.PAUSE_ENTITLEMENT);
        assertEquals(events.get(6).getSubscriptionEventType(), SubscriptionEventType.PAUSE_ENTITLEMENT);

        assertEquals(events.get(7).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        assertEquals(events.get(8).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);

        assertEquals(events.get(0).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(1).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);
        assertEquals(events.get(2).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(3).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);

        assertEquals(events.get(4).getServiceName(), DefaultSubscriptionBundleTimeline.ENT_BILLING_SERVICE_NAME);

        assertEquals(events.get(5).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(6).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);

        assertEquals(events.get(7).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(8).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);

        assertNull(events.get(0).getPrevPhase());
        assertEquals(events.get(0).getNextPhase().getName(), "trial1");
        assertNull(events.get(1).getPrevPhase());
        assertEquals(events.get(1).getNextPhase().getName(), "trial1");

        assertNull(events.get(2).getPrevPhase());
        assertEquals(events.get(2).getNextPhase().getName(), "phase2");
        assertNull(events.get(3).getPrevPhase());
        assertEquals(events.get(3).getNextPhase().getName(), "phase2");

        assertEquals(events.get(4).getPrevPhase().getName(), "trial1");
        assertEquals(events.get(4).getNextPhase().getName(), "phase1");

        assertEquals(events.get(5).getPrevPhase().getName(), "phase1");
        assertEquals(events.get(5).getNextPhase().getName(), "phase1");

        assertEquals(events.get(6).getPrevPhase().getName(), "phase2");
        assertEquals(events.get(6).getNextPhase().getName(), "phase2");

        assertEquals(events.get(7).getPrevPhase().getName(), "phase1");
        assertNull(events.get(7).getNextPhase());

        assertEquals(events.get(8).getPrevPhase().getName(), "phase1");
        assertNull(events.get(8).getNextPhase());
    }

    @Test(groups = "fast")
    public void testWithOverdueOffline() throws CatalogApiException {
        clock.setDay(new LocalDate(2013, 1, 1));

        final DateTimeZone accountTimeZone = DateTimeZone.UTC;
        final UUID accountId = UUID.randomUUID();
        final UUID bundleId = UUID.randomUUID();
        final String externalKey = "foo";

        final UUID entitlementId = UUID.randomUUID();

        final List<SubscriptionBaseTransition> allTransitions = new ArrayList<SubscriptionBaseTransition>();

        final List<BlockingState> blockingStates = new ArrayList<BlockingState>();

        final DateTime requestedDate = new DateTime();
        DateTime effectiveDate = new DateTime(2013, 1, 1, 23, 11, 8, 0, DateTimeZone.UTC);
        final SubscriptionBaseTransition tr1 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CREATE, requestedDate, effectiveDate, clock.getUTCNow(), null, "trial");
        allTransitions.add(tr1);

        effectiveDate = effectiveDate.plusDays(30);
        clock.addDays(30);
        final SubscriptionBaseTransition tr2 = createTransition(entitlementId, EventType.PHASE, null, requestedDate, effectiveDate, clock.getUTCNow(), "trial", "phase");
        allTransitions.add(tr2);

        effectiveDate = effectiveDate.plusDays(40); // 2013-03-12
        clock.addDays(40);
        final SubscriptionBaseTransition tr3 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CANCEL, requestedDate, effectiveDate, clock.getUTCNow(), "phase", null);
        allTransitions.add(tr3);

        final String service = "overdue-service";
        final BlockingState bs1 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.ACCOUNT,
                                                           "OFFLINE", service,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs1);

        final BlockingState bs2 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           DefaultEntitlementApi.ENT_STATE_CANCELLED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs2);

        final List<Entitlement> entitlements = new ArrayList<Entitlement>();
        final Entitlement entitlement = createEntitlement(entitlementId, allTransitions);
        entitlements.add(entitlement);

        final SubscriptionBundleTimeline timeline = new DefaultSubscriptionBundleTimeline(accountTimeZone, accountId, bundleId, externalKey, entitlements, blockingStates);

        assertEquals(timeline.getAccountId(), accountId);
        assertEquals(timeline.getBundleId(), bundleId);
        assertEquals(timeline.getExternalKey(), externalKey);

        final List<SubscriptionEvent> events = timeline.getSubscriptionEvents();
        assertEquals(events.size(), 6);

        assertEquals(events.get(0).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(1).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(2).getEffectiveDate().compareTo(new LocalDate(tr2.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(3).getEffectiveDate().compareTo(new LocalDate(tr3.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(4).getEffectiveDate().compareTo(new LocalDate(tr3.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(5).getEffectiveDate().compareTo(new LocalDate(bs1.getEffectiveDate(), accountTimeZone)), 0);

        assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.PHASE);
        assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);
        assertEquals(events.get(4).getSubscriptionEventType(), SubscriptionEventType.STOP_BILLING);
        assertEquals(events.get(5).getSubscriptionEventType(), SubscriptionEventType.SERVICE_STATE_CHANGE);

        assertEquals(events.get(0).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(1).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);
        assertEquals(events.get(2).getServiceName(), DefaultSubscriptionBundleTimeline.ENT_BILLING_SERVICE_NAME);
        assertEquals(events.get(3).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(4).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);
        assertEquals(events.get(5).getServiceName(), service);

        assertNull(events.get(0).getPrevPhase());
        assertEquals(events.get(0).getNextPhase().getName(), "trial");
        assertNull(events.get(1).getPrevPhase());
        assertEquals(events.get(1).getNextPhase().getName(), "trial");

        assertEquals(events.get(2).getPrevPhase().getName(), "trial");
        assertEquals(events.get(2).getNextPhase().getName(), "phase");

        assertEquals(events.get(3).getPrevPhase().getName(), "phase");
        assertNull(events.get(3).getNextPhase());
        assertEquals(events.get(4).getPrevPhase().getName(), "phase");
        assertNull(events.get(4).getNextPhase());

        assertEquals(events.get(5).getPrevPhase().getName(), "phase");
        assertNull(events.get(5).getNextPhase());
    }

    @Test(groups = "fast", description = "Test for https://github.com/killbill/killbill/issues/134")
    public void testRemoveOverlappingBlockingStates() throws CatalogApiException {
        clock.setDay(new LocalDate(2013, 1, 1));

        final DateTimeZone accountTimeZone = DateTimeZone.UTC;
        final UUID accountId = UUID.randomUUID();
        final UUID bundleId = UUID.randomUUID();
        final String externalKey = "foo";

        final UUID entitlementId = UUID.randomUUID();

        final List<SubscriptionBaseTransition> allTransitions = new ArrayList<SubscriptionBaseTransition>();
        final List<BlockingState> blockingStates = new ArrayList<BlockingState>();

        final DateTime requestedDate = new DateTime();
        DateTime effectiveDate = new DateTime(2013, 1, 1, 15, 43, 25, 0, DateTimeZone.UTC);
        final SubscriptionBaseTransition tr1 = createTransition(entitlementId, EventType.API_USER, ApiEventType.CREATE, requestedDate, effectiveDate, clock.getUTCNow(), null, "trial");
        allTransitions.add(tr1);

        // Overlapping ENT_STATE_BLOCKED - should merge
        effectiveDate = effectiveDate.plusDays(5);
        clock.addDays(5);
        final BlockingState bs1 = new DefaultBlockingState(UUID.randomUUID(), entitlementId, BlockingStateType.SUBSCRIPTION,
                                                           DefaultEntitlementApi.ENT_STATE_BLOCKED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());
        blockingStates.add(bs1);

        effectiveDate = effectiveDate.plusDays(1);
        clock.addDays(1);
        final BlockingState bs2 = new DefaultBlockingState(UUID.randomUUID(), bundleId, BlockingStateType.SUBSCRIPTION_BUNDLE,
                                                           DefaultEntitlementApi.ENT_STATE_BLOCKED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs2);

        // Overlapping ENT_STATE_CANCELLED - should merge
        effectiveDate = effectiveDate.plusDays(1);
        clock.addDays(1);
        final BlockingState bs3 = new DefaultBlockingState(UUID.randomUUID(), accountId, BlockingStateType.ACCOUNT,
                                                           DefaultEntitlementApi.ENT_STATE_CANCELLED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs3);
        final BlockingState bs4 = new DefaultBlockingState(UUID.randomUUID(), bundleId, BlockingStateType.SUBSCRIPTION_BUNDLE,
                                                           DefaultEntitlementApi.ENT_STATE_CANCELLED, DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME,
                                                           true, true, false, effectiveDate, clock.getUTCNow(), clock.getUTCNow());

        blockingStates.add(bs4);

        final List<Entitlement> entitlements = new ArrayList<Entitlement>();
        final Entitlement entitlement = createEntitlement(entitlementId, allTransitions);
        entitlements.add(entitlement);

        final SubscriptionBundleTimeline timeline = new DefaultSubscriptionBundleTimeline(accountTimeZone, accountId, bundleId, externalKey, entitlements, blockingStates);

        final List<SubscriptionEvent> events = timeline.getSubscriptionEvents();
        assertEquals(events.size(), 4);

        assertEquals(events.get(0).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(1).getEffectiveDate().compareTo(new LocalDate(tr1.getEffectiveTransitionTime(), accountTimeZone)), 0);
        assertEquals(events.get(2).getEffectiveDate().compareTo(new LocalDate(bs1.getEffectiveDate(), accountTimeZone)), 0);
        assertEquals(events.get(3).getEffectiveDate().compareTo(new LocalDate(bs3.getEffectiveDate(), accountTimeZone)), 0);

        assertEquals(events.get(0).getSubscriptionEventType(), SubscriptionEventType.START_ENTITLEMENT);
        assertEquals(events.get(1).getSubscriptionEventType(), SubscriptionEventType.START_BILLING);
        assertEquals(events.get(2).getSubscriptionEventType(), SubscriptionEventType.PAUSE_ENTITLEMENT);
        assertEquals(events.get(3).getSubscriptionEventType(), SubscriptionEventType.STOP_ENTITLEMENT);

        assertEquals(events.get(0).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(1).getServiceName(), DefaultSubscriptionBundleTimeline.BILLING_SERVICE_NAME);
        assertEquals(events.get(2).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);
        assertEquals(events.get(3).getServiceName(), DefaultEntitlementService.ENTITLEMENT_SERVICE_NAME);

        assertNull(events.get(0).getPrevPhase());
        assertEquals(events.get(0).getNextPhase().getName(), "trial");
        assertNull(events.get(1).getPrevPhase());
        assertEquals(events.get(1).getNextPhase().getName(), "trial");

        assertEquals(events.get(2).getPrevPhase().getName(), "trial");
        assertEquals(events.get(2).getNextPhase().getName(), "trial");

        assertEquals(events.get(3).getPrevPhase().getName(), "trial");
        assertNull(events.get(3).getNextPhase());
    }

    private Entitlement createEntitlement(final UUID entitlementId, final List<SubscriptionBaseTransition> allTransitions) {
        final DefaultEntitlement result = Mockito.mock(DefaultEntitlement.class);
        Mockito.when(result.getId()).thenReturn(entitlementId);

        final SubscriptionBase base = Mockito.mock(SubscriptionBase.class);
        Mockito.when(base.getAllTransitions()).thenReturn(allTransitions);
        Mockito.when(result.getSubscriptionBase()).thenReturn(base);
        return result;
    }

    private SubscriptionBaseTransition createTransition(final UUID entitlementId,
                                                        final EventType eventType,
                                                        final ApiEventType apiEventType,
                                                        final DateTime requestedDate,
                                                        final DateTime effectiveDate,
                                                        final DateTime createdDate,
                                                        final String prevPhaseName,
                                                        final String nextPhaseName
                                                       ) throws CatalogApiException {
        final PlanPhase prevPhase;
        final Plan prevPlan;
        final Product prevProduct;
        final PriceList prevPriceList;
        if (prevPhaseName == null) {
            prevPhase = null;
            prevPlan = null;
            prevProduct = null;
            prevPriceList = null;
        } else {
            prevPhase = Mockito.mock(PlanPhase.class);
            Mockito.when(prevPhase.getName()).thenReturn(prevPhaseName);

            prevProduct = Mockito.mock(Product.class);
            Mockito.when(prevProduct.getName()).thenReturn("product");

            prevPlan = Mockito.mock(Plan.class);
            Mockito.when(prevPlan.getName()).thenReturn("plan");
            Mockito.when(prevPlan.getProduct()).thenReturn(prevProduct);

            prevPriceList = Mockito.mock(PriceList.class);
            Mockito.when(prevPriceList.getName()).thenReturn("pricelist");
        }

        final PlanPhase nextPhase;
        final Plan nextPlan;
        final Product nextProduct;
        final PriceList nextPriceList;
        if (nextPhaseName == null) {
            nextPhase = null;
            nextPlan = null;
            nextProduct = null;
            nextPriceList = null;
        } else {
            nextPhase = Mockito.mock(PlanPhase.class);
            Mockito.when(nextPhase.getName()).thenReturn(nextPhaseName);

            nextProduct = Mockito.mock(Product.class);
            Mockito.when(nextProduct.getName()).thenReturn("product");

            nextPlan = Mockito.mock(Plan.class);
            Mockito.when(nextPlan.getName()).thenReturn("plan");
            Mockito.when(nextPlan.getProduct()).thenReturn(nextProduct);

            nextPriceList = Mockito.mock(PriceList.class);
            Mockito.when(nextPriceList.getName()).thenReturn("pricelist");
        }

        return new SubscriptionBaseTransitionData(UUID.randomUUID(),
                                                  entitlementId,
                                                  bundleId,
                                                  eventType,
                                                  apiEventType,
                                                  requestedDate,
                                                  effectiveDate,
                                                  null,
                                                  null,
                                                  null,
                                                  prevPlan,
                                                  prevPhase,
                                                  prevPriceList,
                                                  null,
                                                  null,
                                                  null,
                                                  nextPlan,
                                                  nextPhase,
                                                  nextPriceList,
                                                  1L,
                                                  createdDate,
                                                  UUID.randomUUID(),
                                                  true);
    }
}
