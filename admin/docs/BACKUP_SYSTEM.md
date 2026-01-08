# Alfresco Backup System

## Overview
Automated backup system for Alfresco CMS using Babashka script with automatic rotation.

## Current Configuration

### Schedule
- **Frequency**: Every 3 days at 2:00 AM
- **Cron**: `0 2 */3 * *`

### Retention Policy
- **Keep**: 5 backups (approximately 15 days of history)
- **Max Disk Usage**: ~43GB (5 backups × 8.5GB each)

### What Gets Backed Up
1. **Solr indexes** (search functionality)
2. **PostgreSQL database** (all metadata and configurations)
3. **Content store** (all uploaded files and documents)
4. **Configuration files** (docker-compose, environment files)

## File Locations

### Scripts
- **Main backup script**: `/home/tmb/mtzion/mtz-admin/alfresco-backup.clj`
- **Configuration**: `/home/tmb/mtzion/mtz-admin/alfresco-backup-config.edn`

### Backups
- **Backup directory**: `/home/tmb/mtzion/alfresco-backups/daily/`
- **Logs**: `/home/tmb/mtzion/alfresco-backups/logs/`
- **Format**: `alfresco_YYYYMMDD_HHMMSS.tar.gz`

### Cron Job
```bash
# View current cron jobs
crontab -l

# Edit cron jobs
crontab -e
```

Current cron entry:
```
0 2 */3 * * cd /home/tmb/mtzion/mtz-admin && bb alfresco-backup.clj backup >> /home/tmb/mtzion/alfresco-backups/logs/cron.log 2>&1
```

## Manual Backup Operations

### Run Manual Backup
```bash
ssh tmb@trust
cd /home/tmb/mtzion/mtz-admin
bb alfresco-backup.clj backup
```

### List Recent Backups
```bash
ssh tmb@trust
cd /home/tmb/mtzion/mtz-admin
bb alfresco-backup.clj list
```

### Verify Backup System
```bash
ssh tmb@trust
cd /home/tmb/mtzion/mtz-admin
bb alfresco-backup.clj verify
```

### Test Backup (Dry Run)
```bash
ssh tmb@trust
cd /home/tmb/mtzion/mtz-admin
bb alfresco-backup.clj test
```

## Monitoring

### Check Recent Backups
```bash
ssh tmb@trust
ls -lh /home/tmb/mtzion/alfresco-backups/daily/
```

### View Backup Logs
```bash
ssh tmb@trust
tail -f /home/tmb/mtzion/alfresco-backups/logs/cron.log
```

### Check Today's Log
```bash
ssh tmb@trust
cat /home/tmb/mtzion/alfresco-backups/logs/backup-20251105.log
```

### Check Disk Usage
```bash
ssh tmb@trust
du -sh /home/tmb/mtzion/alfresco-backups/daily/*
```

## Automatic Cleanup

The script automatically removes old backups when the count exceeds the retention policy:
- **Current limit**: 5 backups
- **Cleanup trigger**: After each successful backup
- **Method**: Oldest backups are deleted first

## Modifying the Schedule

### Change Backup Frequency

To change from every 3 days to another schedule:

```bash
# Edit crontab
ssh tmb@trust
crontab -e

# Common schedules:
# Daily:        0 2 * * *
# Every 2 days: 0 2 */2 * *
# Every 3 days: 0 2 */3 * *
# Weekly:       0 2 * * 0
```

### Change Retention Count

Edit the configuration file:
```bash
ssh tmb@trust
nano /home/tmb/mtzion/mtz-admin/alfresco-backup-config.edn
```

Modify the `:retention` section:
```clojure
:retention
{:daily 5    ; Number of backups to keep
 :weekly 4
 :monthly 3}
```

## Restoring from Backup

### 1. Extract Backup
```bash
ssh tmb@trust
cd /home/tmb/mtzion/alfresco-backups/daily
tar -xzf alfresco_YYYYMMDD_HHMMSS.tar.gz
```

### 2. Stop Alfresco
```bash
cd /home/tmb/mtzion/alfresco/generated-setup
docker compose down
```

### 3. Restore Database
```bash
docker compose up -d postgres
docker exec -i generated-setup-postgres-1 psql -U alfresco < /path/to/backup/alfresco-database.sql
```

### 4. Restore Content Store
```bash
docker cp /path/to/backup/contentstore/. generated-setup-alfresco-1:/usr/local/tomcat/alf_data/
```

### 5. Restart Alfresco
```bash
docker compose up -d
```

## Troubleshooting

### Backup Not Running
1. Check cron is running: `systemctl status cron`
2. Check cron logs: `tail /home/tmb/mtzion/alfresco-backups/logs/cron.log`
3. Verify containers are running: `docker ps`

### Backup Failed
1. Check today's log: `cat /home/tmb/mtzion/alfresco-backups/logs/backup-20251105.log`
2. Verify disk space: `df -h`
3. Test manually: `bb alfresco-backup.clj test`

### Disk Full
1. Check backup size: `du -sh /home/tmb/mtzion/alfresco-backups/daily`
2. Reduce retention count in config
3. Manually delete old backups if needed

## Best Practices

1. **Monitor regularly**: Check logs weekly to ensure backups are succeeding
2. **Test restores**: Periodically test backup restoration on a test system
3. **Off-site copies**: Consider copying backups to another server or cloud storage
4. **Disk space**: Monitor disk usage to ensure backups don't fill the drive
5. **Retention balance**: Balance between history needs and disk space

## Change History

- **2025-11-05**: Changed from daily to every 3 days (better for slow-changing data)
- **2025-11-05**: Reduced retention from 7 to 5 backups (15 days of history)
