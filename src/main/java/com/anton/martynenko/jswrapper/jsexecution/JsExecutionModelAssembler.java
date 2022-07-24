package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.enums.Property;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * RepresentationModelAssembler bean. Converts {@link JsExecution} entity into HATEOAS {@link EntityModel} containing links.
 * @author Martynenko Anton
 * @since 1.2
 */

@Component
public class JsExecutionModelAssembler implements RepresentationModelAssembler<JsExecution, EntityModel<JsExecution>> {

  @Override
  @NotNull
  public EntityModel<JsExecution> toModel(@NotNull final JsExecution jsExecution) {
    List<Link> links = new ArrayList<>();

    links.add(linkTo(methodOn(JsExecutionController.class).getJsExecution(jsExecution.getId())).withSelfRel());
    links.add(linkTo(methodOn(JsExecutionController.class).listJsExecutions(null, null)).withRel("jsExecutions"));

    if (jsExecution.isCancelable()) {
      Link link = linkTo(methodOn(JsExecutionController.class)
          .cancelJsExecution(jsExecution.getId())).withRel("cancel");
      links.add(link);
    }

    for (Property property: Property.values()) {
      Link link = linkTo(methodOn(JsExecutionController.class)
          .getExecutionDetails(jsExecution.getId(), property)).withRel(property.name());
      links.add(link);
    }

    return EntityModel.of(jsExecution, links);
  }
}
