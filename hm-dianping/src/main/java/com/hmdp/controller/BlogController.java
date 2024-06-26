package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.hmdp.manager.BlogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogManager blogManager;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogManager.saveBlog(blog);
    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogManager.likeBlog(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogManager.queryMyBlog(current);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogManager.queryHotBlog(current);
    }

    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id) {
        return blogManager.queryBlogById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id) {
        return blogManager.queryBlogLikes(id);
    }

    // BlogController
    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        return blogManager.queryBlogByUserId(current, id);
    }

    @GetMapping("of/follow")
    public Result queryBlogByFollow(@RequestParam("lastId") Long lastId,
                                    @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return blogManager.queryBlogByFollow(lastId, offset);
    }
}
