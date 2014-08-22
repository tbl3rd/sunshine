ANDROID := $(ANDROID_HOME)/tools/android
ADB := $(ANDROID_HOME)/platform-tools/adb

API := android-19

ANDROIDAPIS := $(API) # Add others as required.

ANDROIDSOURCES := $(addprefix $(ANDROID_HOME)/sources/,$(ANDROIDAPIS))

PACKAGE := com.example.android.sunshine

# Task names extracted from output of './gradlew tasks' and re-ordered
# slightly.
#
GRADLETASKS :=\
	help\
	tasks\
	androidDependencies\
	signingReport\
	assemble\
	assembleDebug\
	assembleDebugTest\
	assembleRelease\
	build\
	buildDependents\
	buildNeeded\
	clean\
	init\
	wrapper\
	dependencies\
	dependencyInsight\
	projects\
	properties\
	installDebug\
	installDebugTest\
	uninstallAll\
	uninstallDebug\
	uninstallDebugTest\
	uninstallRelease\
	check\
	connectedAndroidTest\
	connectedCheck\
	deviceCheck\
	#


$(GRADLETASKS):
	./gradlew $@
.PHONY: $(GRADLETASKS)


debug: installDebug
	test "$(ANDROID_USB)" && $(ADB) usb || true
	$(ADB) logcat -c
	$(ADB) shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity
	$(ADB) logcat
.PHONY: debug


INSTRUMENT := $(PACKAGE).test/android.test.InstrumentationTestRunner

test: assembleDebugTest
	@echo
	@echo Look for: Test results for InstrumentationTestRunner=.....
	@echo Look for: 'OK (5 tests)' ... for some value of 5
	@echo
	test "$(ANDROID_USB)" && $(ADB) usb || true
	$(ADB) logcat -c
	$(ADB) shell pm install -r /data/local/tmp/$(PACKAGE)
	$(ADB) shell pm install -r /data/local/tmp/$(PACKAGE).test
	$(ADB) shell pm list instrumentation
	$(ADB) shell am instrument -w -e target $(PACKAGE) $(INSTRUMENT)
	$(ADB) shell pm uninstall $(PACKAGE).test
	$(ADB) shell pm uninstall $(PACKAGE)
	$(ADB) logcat
.PHONY: test


bugreport:
	$(ADB) bugreport
.PHONY: bugreport


lint lintDebug lintRelease:
	./gradlew $@
	open app/build/outputs/lint-results.html
.PHONY: lint lintDebug lintRelease


define TAGJAVA
find $(1) -type f -name '*.java' -print |\
xargs etags --append --output=$(2) &&
endef
TAGS tags: . $(LIBRARIES) $(ANDROIDSOURCES)
	rm -f TAGS.tmp
	$(foreach d,$^,$(call TAGJAVA,$(d),TAGS.tmp)) true
	cmp -s ./TAGS TAGS.tmp || rm -f ./TAGS && mv TAGS.tmp ./TAGS
.PHONY: TAGS tags


distclean: clean
	rm -f TAGS
.PHONY: distclean
