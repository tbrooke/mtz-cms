# Alfresco imgpreview Rendition - Test Plan

**Status**: Ready for Testing
**Issue**: mtz-cms-16
**Date**: October 18, 2025

## Summary

All images (Hero, Features, Blog) now use Alfresco's imgpreview rendition (960px width) for optimized loading. This should significantly reduce file sizes and improve page load performance.

## What Changed

1. ✅ Added `get-image-rendition` function in `alfresco/client.clj`
2. ✅ Updated image-proxy-handler to support renditions
3. ✅ New routes: `/proxy/image/:node-id/:rendition`
4. ✅ All image URLs now use `/proxy/image/:node-id/imgpreview`
5. ✅ Consolidated from `/api/image/` to `/proxy/image/`

## Test Steps

### 1. **Restart Server** (REQUIRED)

The code changes require a server restart:

```bash
# Stop current server (Ctrl+C if running)
# Then start fresh:
clj -M:dev
```

### 2. **Test Alfresco Renditions**

First, verify Alfresco has imgpreview renditions available:

```bash
# Run the test script:
./admin/scripts/test_imgpreview.clj
```

Expected output:
- ✅ Should find images in Hero folder
- ✅ imgpreview should be available (960px width)
- ⚠️  If imgpreview not available, Alfresco needs to generate it (may take a few minutes after upload)

### 3. **Test Homepage Hero**

1. Open browser: http://localhost:3000
2. Open DevTools → Network tab
3. Refresh page
4. Look for image requests

**Expected**:
- ✅ Hero images load from `/proxy/image/[NODE-ID]/imgpreview`
- ✅ File sizes should be 100-300 KB (not 1-2+ MB)
- ✅ Images display correctly with no cropping
- ✅ Aspect ratio maintained (16:9)

**Before vs After**:
```
Before: /proxy/image/abc123 → 2.5 MB (3300x5100)
After:  /proxy/image/abc123/imgpreview → 250 KB (960px width)
```

### 4. **Test Feature Cards**

1. Scroll to feature cards section on homepage
2. Check Network tab

**Expected**:
- ✅ Feature images load from `/proxy/image/[NODE-ID]/imgpreview`
- ✅ Images display correctly (no cutoff)
- ✅ Aspect ratio maintained (11:17)
- ✅ Card heights look correct (1200px total)

### 5. **Test Blog Thumbnails**

1. Navigate to: http://localhost:3000/blog
2. Check Network tab

**Expected**:
- ✅ Blog thumbnails load from `/proxy/image/[NODE-ID]/imgpreview`
- ✅ Portrait images not cropped
- ✅ Smaller file sizes

### 6. **Test PDF Thumbnails**

1. Navigate to: http://localhost:3000/worship/sunday
2. Check bulletin thumbnails

**Expected**:
- ✅ PDF thumbnails load from `/proxy/image/[NODE-ID]` (no rendition param)
- ✅ Alfresco automatically uses PDF thumbnail renditions
- ✅ Thumbnails display correctly

### 7. **Test Caching**

1. Refresh page multiple times
2. Check Network tab

**Expected**:
- ✅ First load: 200 (fetched from Alfresco)
- ✅ Subsequent loads: 304 (from cache) or instant (browser cache)
- ✅ Cache headers: `Cache-Control: public, max-age=3600`

### 8. **Test Fallback**

If imgpreview rendition doesn't exist, system should fallback to original:

1. Create new folder in Alfresco
2. Upload new image
3. Check if it loads immediately

**Expected**:
- ✅ Loads original if imgpreview not yet generated
- ⚠️  Original will be larger, but page still works
- ✅ After a few minutes, Alfresco generates imgpreview automatically

## Performance Comparison

### Before (Original Images)
```
Hero image:     3300x5100 = 2.5 MB
Feature image:  3300x5100 = 2.8 MB
Total 3 features: ~8.4 MB
Page load: 10-15 seconds
```

### After (imgpreview Renditions)
```
Hero image:     960x540 = 180 KB
Feature image:  960x1482 = 250 KB
Total 3 features: ~750 KB
Page load: 2-3 seconds
```

**Expected improvement**: 10-12x smaller file sizes, 5-7x faster page loads

## Troubleshooting

### Problem: imgpreview not available
**Solution**:
- Alfresco generates renditions asynchronously
- Wait 2-5 minutes after uploading image
- Check: `/alfresco/api/-default-/public/alfresco/versions/1/nodes/[NODE-ID]/renditions`

### Problem: Images still loading slowly
**Check**:
- Network tab shows `/imgpreview` in URL?
- File sizes under 500 KB?
- SSH tunnel to Alfresco still running?

### Problem: Images not displaying
**Check**:
- Server restarted after code changes?
- Alfresco connection working? (test with `/admin/alfresco-test`)
- Check browser console for errors

### Problem: Images look pixelated
**Solution**:
- Designer should export at 1920x1080 (Hero) or 1200x1854 (Features)
- imgpreview at 960px should look crisp on most screens
- For retina displays, 960px is adequate (2x density = 1920px effective)

## Next Steps After Testing

### If Tests Pass ✅
1. Close Beads issue: `bd close mtz-cms-16`
2. Update designer on export sizes:
   - Hero: 1920x1080 (16:9)
   - Features: 1200x1854 (11:17)
   - Blog authors: 800x1200 (portrait)
3. Consider adding more rendition sizes in future if needed

### If Tests Fail ❌
1. Document what's not working
2. Check logs for errors
3. Verify Alfresco renditions are being generated
4. Test individual endpoints with curl

## Test Results

**Tester**: _______________
**Date**: _______________
**Server Version**: _______________

- [ ] Server restarted successfully
- [ ] Alfresco imgpreview renditions available
- [ ] Hero images load with imgpreview
- [ ] Feature images load with imgpreview
- [ ] Blog thumbnails load with imgpreview
- [ ] PDF thumbnails load correctly
- [ ] File sizes significantly reduced
- [ ] Page load performance improved
- [ ] Images display correctly (no cropping/distortion)
- [ ] Caching works correctly
- [ ] Fallback to original works if rendition unavailable

**Overall Result**: ⬜ PASS  ⬜ FAIL

**Notes**:
_____________________________________________
_____________________________________________
_____________________________________________
