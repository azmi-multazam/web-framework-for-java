package net.bndy.wf.modules.cms.services;

import net.bndy.lib.StringHelper;
import net.bndy.wf.exceptions.NoResourceFoundException;
import net.bndy.wf.modules.cms.models.Channel;
import net.bndy.wf.modules.cms.models.Page;
import net.bndy.wf.modules.cms.services.repositories.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Transactional
public class ChannelService extends _BaseService<Channel> {

    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private PageService pageService;

    private void syncVisible(@NotNull Channel channel) {
        if (channel.isVisible()) {
            List<Long> ids = StringHelper.splitToLong(channel.getPath(), "/");
            if (ids.size() > 0) {
                this.channelRepository.openVisible(ids);
            }
        } else {
            this.channelRepository.hideAllChildren(channel.getPath() + channel.getId() + "/");
        }
    }

    public void toggleVisible(long channelId) {
        Channel channel = this.channelRepository.findOne(channelId);
        if (channel != null) {
            channel.setVisible(!channel.isVisible());
            channel = this.channelRepository.saveAndFlush(channel);
            syncVisible(channel);
        }
    }

    public int countChildren(long channelId) throws NoResourceFoundException {
        Channel channel = this.channelRepository.findOne(channelId);
        if (channel != null) {
            return this.channelRepository.countByPath(channel.getPath() + channel.getId() + "/");
        }

        throw new NoResourceFoundException();
    }

    public void transferChannel(long sourceId, long targetId) {
        Channel source = this.channelRepository.findOne(sourceId);
        Channel target = this.channelRepository.findOne(targetId);
        if (source != null && target != null && source.getBoType() == target.getBoType()) {
            // TODO: transfer BO according to boType
            switch (source.getBoType()) {
                case File:
                    break;
                case Page:
                    this.pageService.transfer(source.getId(), target.getId());
                    break;
                case Article:
                    this.articleService.transfer(source.getId(), target.getId());
                    break;
            }
        }
    }

    @Override
    public Channel save(Channel entity) {
        if (entity.getPath() == null || "".equals(entity.getPath())) {
            entity.setPath("/");
        }
        entity = super.save(entity);
        syncVisible(entity);

        switch (entity.getBoType()) {
            case Page:
                Page p = this.pageService.getByChannelId(entity.getId());
                if (p == null) {
                    p = new Page();
                    p.setContent(entity.getName());
                }
                p.setChannelId(entity.getId());
                p.setTitle(entity.getName());
                this.pageService.save(p);
                break;
        }

        return entity;
    }

    @Override
    public boolean delete(long id) {
        Channel channel = this.get(id);
        if (channel != null) {
            if (channel.getBoType() != null) {
                switch (channel.getBoType()) {
                    case Article:
                        this.articleService.deleteByChannelId(channel.getId());
                        break;
                    case Page:
                        this.pageService.deleteByChannelId(channel.getId());
                        break;
                    case File:
                        // TODO: remove File channel
                        break;
                }
            }
            return super.delete(id);
        }
        return false;
    }
}
