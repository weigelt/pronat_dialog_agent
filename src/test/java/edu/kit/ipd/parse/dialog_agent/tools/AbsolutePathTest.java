package edu.kit.ipd.parse.dialog_agent.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AbsolutePathTest {

	@Test(expected = DirectoryAccessibilityException.class)
	public void testCheckAvailabilityOfAbsolutePath1() {
		AbsolutePath absPath = new AbsolutePath("DialogManager/goal");
	}
	
	@Test
	public void testCheckAvailabilityOfAbsolutePath2() {
		AbsolutePath absPath = new AbsolutePath("/DialogManager/targetDirecty/");
		String testPath = "/Users/Mario/DialogManager/targetDirecty/";
		assertEquals(absPath.getAbsolutePathDirectory(), testPath);
	}
	
	@Test
	public void testCheckAvailabilityOfAbsolutePath3() {
		AbsolutePath absPath = new AbsolutePath("/DialogManager/targetDirecty/");
		String testPath = "/Users/Mario/DialogManager/targetDirecty/audioFile.flac";
		assertEquals(absPath.getAbsolutePathFile("audioFile", "flac"), testPath);
	}
}
